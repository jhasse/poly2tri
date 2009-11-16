#
# Poly2Tri
# Copyright (c) 2009, Mason Green
# http://code.google.com/p/poly2tri/
# 
# All rights reserved.
# 
# Redistribution and use in source and binary forms, with or without modification,
# are permitted provided that the following conditions are met:
# 
# Redistributions of source code must retain the above copyright notice,
# self list of conditions and the following disclaimer.
# Redistributions in binary form must reproduce the above copyright notice,
# self list of conditions and the following disclaimer in the documentation
# and/or other materials provided with the distribution.
# Neither the name of Poly2Tri nor the names of its contributors may be
# used to endorse or promote products derived from self software without specific
# prior written permission.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
# A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
# CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
# EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
# PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
# PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
# LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
# NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
# 
from random import shuffle

###
### Based on Raimund Seidel'e paper "A simple and fast incremental randomized
### algorithm for computing trapezoidal decompositions and for triangulating polygons"
### (Ported from poly2tri)

cdef extern from 'math.h':
    double cos(double)
    double sin(double)
    double atan2(double, double)
    double floor(double)
    double sqrt(double)

class Triangulator:
  
    def __init__(self, points):
        self.polygons = []
        self.edge_list = self.init_edges(points)
        self.trapezoids = []
        self.trapezoidal_map = TrapezoidalMap()
        bounding_box = self.trapezoidal_map.bounding_box(self.edge_list)
        self.query_graph = QueryGraph(Sink(bounding_box))
        self.xmono_poly = []
        self.process()
  
    def trapezoidMap(self): 
        return self.trapezoidal_map.map
    
    # Build the trapezoidal map and query graph
    def process(self):
        for e in self.edge_list:
            traps = self.query_graph.follow_edge(e)
            for t in traps:
                try:
                    self.trapezoidal_map.map.remove(t)
                except:
                    pass
            for t in traps:
                tlist = []
                cp = t.contains(e.p)
                cq = t.contains(e.q)
                if cp and cq:
                    tlist = self.trapezoidal_map.case1(t, e)
                    self.query_graph.case1(t.sink, e, tlist)
                elif cp and not cq:
                    tlist = self.trapezoidal_map.case2(t, e) 
                    self.query_graph.case2(t.sink, e, tlist)
                elif not cp and not cq:
                    tlist = self.trapezoidal_map.case3(t, e)
                    self.query_graph.case3(t.sink, e, tlist)
                else:
                    tlist = self.trapezoidal_map.case4(t, e)
                    self.query_graph.case4(t.sink, e, tlist)
                
                # Add new trapezoids to map
                for t in tlist:
                  self.trapezoidal_map.map.append(t)
          
            self.trapezoidal_map.clear()

        # Mark outside trapezoids
        for t in self.trapezoidal_map.map:
            self.mark_outside(t)
        
        # Collect interior trapezoids
        for t in self.trapezoidal_map.map:
            if t.inside():
                self.trapezoids.append(t)
                t.add_points()

        self.create_mountains()

    def mono_polies(self):
        polies = []
        for x in self.xmono_poly:
            polies.append(x.monoPoly)
        return polies
  
    def create_mountains(self):
        for s in self.edge_list:          
            if len(s.mpoints) > 0:
                mountain = MonotoneMountain()
                k = merge_sort(s.mpoints)
                points = [s.p] + k + [s.q]
                for p in points:
                    mountain.append(p)
                mountain.process()
                for t in mountain.triangles:
                    self.polygons.append(t)
                self.xmono_poly.append(mountain)
  
    def mark_outside(self, t):
        if t.top is self.bounding_box.top or t.bottom is self.bounding_box.bottom:
            t.trimNeighbors()
  
    def init_edges(self, points):
        edges = []
        for i in range(len(points)-1):
            edges.append(Edge(points[i], points[i+1]))
        edges.append(Edge(points[0], points[-1]))
        return self.order_edges(edges)
  
    def order_edges(self, edges):
        segs = []
        for s in edges:
            p = self.shearTransform(s.p)
            q = self.shearTransform(s.q)
            if p.x > q.x: segs.append(Edge(q, p))
            elif p.x < q.x: segs.append(Edge(p, q))
        shuffle(segs)
        return segs

    def shearTransform(self, point):
        return Point(point.x + 1e-4 * point.y, point.y)
 
cdef list merge_sort(list l):
    cdef list lleft, lright
    cdef int p1, p2, p
    if len(l)>1 :
        lleft = merge_sort(l[:len(l)/2])
        lright = merge_sort(l[len(l)/2:])
        p1, p2, p = 0, 0, 0
        while p1<len(lleft) and p2<len(lright):
            if lleft[p1].x < lright[p2].x:
                l[p]=lleft[p1]
                p+=1
                p1+=1
            else:
                l[p]=lright[p2]
                p+=1
                p2+=1
        if p1<len(lleft):l[p:]=lleft[p1:]
        elif p2<len(lright):l[p:]=lright[p2:]
        else : print "internal error"
    return l
  
cdef class Point:
  
    cdef float x, y
    
    next = None
    prev = None
    edge = None
    edges = []
    
    property x:
        def __get__(self): return self.x
    
    property y:
        def __get__(self): return self.y
        
    def __init__(self, float x, float y):
        self.x = x
        self.y = y

    def __sub__(self, other):
        if isinstance(other, Point):
            return Point(self.x - other.x, self.y - other.y) 
        else:
            return Point(self.x - other, self.y - other)
    
    def __add__(self, other):
        if isinstance(other, Point):
            return Point(self.x + other.x, self.y + other.y) 
        else:
            return Point(self.x + other, self.y + other)
    
    def __mul__(self, float f):
        return Point(self.x * f, self.y * f)
    
    def __div__(self, float a):
        return Point(self.x / a, self.y / a)
    
    def cross(self, Point p):
        return self.x * p.y - self.y * p.x
    
    def dot(self, Point p):
        return self.x * p.x + self.y * p.y
    
    def length(self):
        return sqrt(self.x * self.x + self.y * self.y)
        
    def normalize(self):
        return self / self.length() 
        
    def less(self, Point p):
        return self.x < p.x
  
    '''
    # Sort along y axis
    def greater(self, p):
        if y < p.y: 
          return True
        elif y > p.y:
          return False
        else:
          if x < p.x:
            return True
          else:
            return False
    '''
    
    def not_equal(self, p):
        return not (p.x == self.x and p.y == self.y)
        
    def clone(self):
        return Point(self.x, self.y)
  
cdef class Edge:

    cdef Point p, q
    cdef bool above, below
    cdef float slope, b
 
    mpoints = []
    
    def __init__(self, Point p, Point q):
        self.p = p
        self.q = q
        self.slope = (q.y - p.y)/(q.x - p.x)
        self.b = p.y - (p.x * self.slope)
  
    property p:
        def __get__(self): return self.p
        
    property q:
        def __get__(self): return self.q
        
    property above:
        def __get__(self): return self.above
        
    property below:
        def __get__(self): return self.below
        
    cdef bool is_above(self, Point point):
        return (floor(point.y) < floor(self.slope * point.x + self.b))
    cdef bool is_below(self, Point point):
        return (floor(point.y) > floor(self.slope * point.x + self.b))

    cdef float intersect(self, Point c, Point d):
        cdef float a1, a2, a3, a4, t
        cdef Point a, b
        a = self.p
        b = self.q
        a1 = self.signed_area(a, b, d)
        a2 = self.signed_area(a, b, c)
        if  a1 != 0 and a2 != 0 and (a1 * a2) < 0:
            a3 = self.signed_area(c, d, a)
            a4 = a3 + a2 - a1
            if a3 * a4 < 0:
                t = a3 / (a3 - a4)
                return a + ((b - a) * t)
        return 0.0
        
    cdef float signed_area(self, Point a, Point b, Point c):
        return (a.x - c.x) * (b.y - c.y) - (a.y - c.y) * (b.x - c.x)

cdef Point line_intersect(Edge e, float x):
    cdef float y =  e.slope * x + e.b
    return Point(x, y)
        
cdef class Trapezoid:

    cdef:
        Point left_point, right_point
        Edge top, bottom
        Trapezoid upper_left, lower_left
        Trapezoid upper_right, lower_right
        bool inside
        object sink
    
    def __init__(self, Point left_point, Point right_point, Edge top, Edge bottom):
        self.left_point = left_point
        self.right_point = right_point
        self.top = top
        self.bottom = bottom
        self.upper_left = None
        self.upper_right = None
        self.lower_left = None
        self.lower_right = None
        self.inside = True
        self.sink = None
    
    property top:
        def __get__(self): return self.top
        
    property bottom:
        def __get__(self): return self.bottom
        
    property left_point:
        def __get__(self): return self.left_point
    
    property right_point:
        def __get__(self): return self.right_point
        
    property sink:
        def __get__(self): return self.sink
        def __set__(self, object s): self.sink = s
    
    property upper_left:
        def __get__(self): return self.upper_left
        def __set__(self, Trapezoid other): self.upper_left = other
        
    property upper_right:
        def __get__(self): return self.upper_right
        def __set__(self, Trapezoid other): self.upper_right = other

    property lower_left:
        def __get__(self): return self.lower_left
        def __set__(self, Trapezoid other): self.lower_left = other
        
    property lower_right:
        def __get__(self): return self.lower_right
        def __set__(self, Trapezoid other): self.lower_right = other
        
    def update_left(self, Trapezoid ul, Trapezoid ll):
        self.upper_left = ul
        self.lower_left = ll
        if ul != None: ul.upper_right = self
        if ll != None: ll.lower_right = self  
  
    def update_right(self, Trapezoid ur, Trapezoid lr):
        self.upper_right = ur
        self.lower_right = lr
        if ur != None: ur.upper_left = self
        if lr != None: lr.lower_left = self   

    def update_left_right(self, Trapezoid ul, Trapezoid ll, Trapezoid ur, Trapezoid lr):
        self.upper_left = ul
        self.lower_left = ll 
        self.upper_right = ur 
        self.lower_right = lr   
        if ul != None: ul.upper_right = self
        if ll != None: ll.lower_right = self
        if ur != None: ur.upper_left = self
        if lr != None: lr.lower_left = self  
        
    def trim_neighbors(self):
        if self.inside:
            self.inside = False
            if self.upper_left != None: self.upper_left.trim_neighbors()
            if self.lower_left != None: self.lower_left.trim_neighbors()
            if self.upper_right != None: self.upper_right.trim_neighbors()
            if self.lower_right != None: self.lower_right.trim_neighbors()
  
    def contains(self, Point point):
        return (point.x > self.left_point.x and point.x < self.right_point.x and 
                self.top.is_above(point) and self.bottom.is_below(point))
  
    def vertices(self):
        cdef list verts = []
        verts.append(line_intersect(self.top, self.left_point.x))
        verts.append(line_intersect(self.bottom, self.left_point.x))
        verts.append(line_intersect(self.bottom, self.right_point.x))
        verts.append(line_intersect(self.top, self.right_point.x))
        return verts
  
    def add_points(self):
        if self.left_point != self.bottom.p: 
            self.bottom.mpoints.append(self.left_point.clone)
        if self.right_point != self.bottom.q: 
            self.bottom.mpoints.append(self.right_point.clone)
        if self.left_point != self.top.p: 
            self.top.mpoints.append(self.left_point.clone)
        if self.right_point != self.top.q: 
            self.top.mpoints.append(self.right_point.clone)

class TrapezoidalMap:

    map = []
    margin = 50
    bcross = None
    tcross = None

    def clear(self):
        self.bcross = None
        self.tcross = None

    def case1(self, t, e):
        trapezoids = []
        trapezoids.append(Trapezoid(t.left_point, e.p, t.top, t.bottom))
        trapezoids.append(Trapezoid(e.p, e.q, t.top, e))
        trapezoids.append(Trapezoid(e.p, e.q, e, t.bottom))
        trapezoids.append(Trapezoid(e.q, t.right_point, t.top, t.bottom))
        trapezoids[0].update_left(t.upper_left, t.lower_left)
        trapezoids[1].update_left_right(trapezoids[0], None, trapezoids[3], None)
        trapezoids[2].update_left_right(None, trapezoids[0], None, trapezoids[3])
        trapezoids[3].update_right(t.upper_right, t.lower_right)
        return trapezoids

    def case2(self, t, e):
        rp = e.q if e.q.x == t.right_point.x else t.right_point
        trapezoids = []
        trapezoids.append(Trapezoid(t.left_point, e.p, t.top, t.bottom))
        trapezoids.append(Trapezoid(e.p, rp, t.top, e))
        trapezoids.append(Trapezoid(e.p, rp, e, t.bottom))
        trapezoids[0].update_left(t.upper_left, t.lower_left)
        trapezoids[1].update_left_right(trapezoids[0], None, t.upper_right, None)
        trapezoids[2].update_left_right(None, trapezoids[0], None, t.lower_right)
        self.bcross = t.bottom
        self.tcross = t.top
        e.above = trapezoids[1]
        e.below = trapezoids[2]
        return trapezoids
  
    def case3(self, t, e):
        lp = e.p if e.p.x == t.left_point.x  else t.left_point
        rp = e.q if e.q.x == t.right_point.x else t.right_point
        trapezoids = []
        if self.tcross is t.top:
            trapezoids.append(t.upper_left)
            trapezoids[0].update_right(t.upper_right, None)
            trapezoids[0].right_point = rp
        else:
            trapezoids.append(Trapezoid(lp, rp, t.top, e))
            trapezoids[0].update_left_right(t.upper_left, e.above, t.upper_right, None)
        if self.bcross is t.bottom:
            trapezoids.append(t.lower_left)
            trapezoids[1].update_right(None, t.lower_right)
            trapezoids[1].right_point = rp
        else:
            trapezoids.append(Trapezoid(lp, rp, e, t.bottom))
            trapezoids[1].update_left_right(e.below, t.lower_left, None, t.lower_right)
        self.bcross = t.bottom
        self.tcross = t.top
        e.above = trapezoids[0]
        e.below = trapezoids[1]
        return trapezoids

    def case4(self, t, e):
        lp = e.p if e.p.x == t.left_point.x else t.left_point
        trapezoids = []
        if self.tcross is t.top:
            trapezoids.append(t.upper_left)
            trapezoids[0].right_point = e.q
        else:
            trapezoids.append(Trapezoid(lp, e.q, t.top, e))
            trapezoids[0].update_left(t.upper_left, e.above)
        if self.bcross is t.bottom:
            trapezoids.append(t.lower_left)
            trapezoids[1].right_point = e.q
        else:
            trapezoids.append(Trapezoid(lp, e.q, e, t.bottom))
            trapezoids[1].update_left(e.below, t.lower_left)
        trapezoids.append(Trapezoid(e.q, t.right_point, t.top, t.bottom))
        trapezoids[2].update_left_right(trapezoids[0], trapezoids[1], t.upper_right, t.lower_right)
        
        return trapezoids
  
    def bounding_box(self, edges): 
        margin = self.margin
        max = edges[0].p + margin
        min = edges[0].q - margin
        for e in edges:
            if e.p.x > max.x: max = Point(e.p.x + margin, max.y)
            if e.p.y > max.y: max = Point(max.x, e.p.y + margin)
            if e.q.x > max.x: max = Point(e.q.x + margin, max.y)
            if e.q.y > max.y: max = Point(max.x, e.q.y + margin)
            if e.p.x < min.x: min = Point(e.p.x - margin, min.y)
            if e.p.y < min.y: min = Point(min.x, e.p.y - margin)
            if e.q.x < min.x: min = Point(e.q.x - margin, min.y)
            if e.q.y < min.y: min = Point(min.x, e.q.y - margin)
        top = Edge(Point(min.x, max.y), Point(max.x, max.y))
        bottom = Edge(Point(min.x, min.y), Point(max.x, min.y))
        left = bottom.p
        right = top.q
        return Trapezoid(left, right, top, bottom)

class Node:

    parent_list = []
    
    def __init__(self, left, right):
        self.left = left
        self.right = right
        if left != None: left.parent_list.append(self)
        if right != None: right.parent_list.append(self)
  
    def replace(self, node):
        for parent in node.parent_list:
            if parent.left is node: 
                parent.left = self
            else: 
                parent.right = self 
            self.parent_list.append(parent)

class Sink(Node):

    def __new__(cls, trapezoid):
        if trapezoid.sink != None: 
            return trapezoid.sink
        return Sink(trapezoid)
            
    def __init__(self, trapezoid):
        self.trapezoid = trapezoid
        Node.__init__(self, None, None)
        trapezoid.sink = self
  
    def locate(self, e): 
        return self
        
class XNode(Node):

    def __init__(self, point, lchild, rchild):
        Node.__init__(self, lchild, rchild)
        self.point = point
        self.lchild = lchild
        self.rchild = rchild
    
    def locate(self, e): 
        if e.p.x >= self.point.x: 
            return self.right.locate(e)
        return self.left.locate(e)

class YNode(Node):

    def __init__(self, edge, lchild, rchild):
        Node.__init__(self, lchild, rchild)
        self.edge = edge
        self.lchild = lchild
        self.rchild = rchild
        
    def locate(self, e):
        if self.edge.is_above(e.p): 
            return self.right.locate(e)
        elif self.edge.is_below(e.p): 
            return self.left.locate(e)
        else:
            if e.slope < self.edge.slope: 
                return self.right.locate(e)
        return self.left.locate(e)
            
class QueryGraph:

    head = None
    
    def __init__(self, head):
        self.head = head
        
    def locate(self, e):
        return self.head.locate(e).trapezoid
  
    def follow_edge(self, e):
        trapezoids = [self.locate(e)]
        j = 0
        while(e.q.x > trapezoids[j].right_point.x):
            if e > trapezoids[j].right_point:
                trapezoids.append(trapezoids[j].upper_right)
            else:
                trapezoids .append(trapezoids[j].lower_right)
            j += 1
        return trapezoids
  
    def replace(self, sink, node):
        if not sink.parent_list:
          self.head = node
        else:
          node.replace(sink)

    def case1(self, sink, e, tlist):
        yNode = YNode(e, Sink(tlist[1]), Sink(tlist[2]))
        qNode = XNode(e.q, yNode, Sink(tlist[3]))
        pNode = XNode(e.p, Sink(tlist[0]), qNode)
        self.replace(sink, pNode)
  
    def case2(self, sink, e, tlist):
        yNode = YNode(e, Sink(tlist[1]), Sink(tlist[2]))
        pNode = XNode(e.p, Sink(tlist[0]), yNode)
        self.replace(sink, pNode)
  
    def case3(self, sink, e, tlist):
        yNode = YNode(e, Sink(tlist[0]), Sink(tlist[1]))
        self.replace(sink, yNode)

    def case4(self, sink, e, tlist):
        yNode = YNode(e, Sink(tlist[0]), Sink(tlist[1]))
        qNode = XNode(e.q, yNode, Sink(tlist[2]))
        self.replace(sink, qNode)

cdef float PI_SLOP = 3.1

cdef class MonotoneMountain:

    cdef:
        Point tail, head
        int size
        list convex_points
        list mono_poly
        list triangles
        list convex_polies                        
        bool positive

    def __init__(self):
        self.size = 0
        self.tail, self.head = None
        self.positive = False
        self.convex_points = []
        self.mono_poly = []
        self.triangles = []
        self.convex_polies = []
        
    def append(self, Point point):
        if self.size == 0: 
            self.head = point
            self.size += 1
        elif self.size == 1:
            if point.not_equal(self.head):
                self.tail = point
                self.tail.prev = self.head
                self.head.next = self.tail
                self.size += 1
        else:
            if point.not_equal(self.tail):
                self.tail.next = point
                point.prev = self.tail
                self.tail = point
                self.size += 1

    cdef void remove(self, Point point):
        cdef Point next, prev
        next = point.next
        prev = point.prev
        point.prev.next = next
        point.next.prev = prev
        self.size -= 1

    def process(self):
        self.positive = self.angle_sign()
        self.gen_mono_poly()
        p = self.head.next
        while p != self.tail:
            a = self.angle(p)
            if a >= PI_SLOP or a <= -PI_SLOP: self.remove(p)
            elif self.is_convex(p): self.convex_points.append(p)
            p = p.next
        self.triangulate()

    cdef void triangulate(self):
        while not len(self.convex_points) > 0:
            ear = self.convex_points.remove(0)
            a = ear.prev
            b = ear
            c = ear.next
            triangle = [a, b, c]
            self.triangles.append(triangle)
            self.remove(ear)
            if self.valid(a): self.convex_points.append(a)
            if self.valid(c): self.convex_points.append(c)
        assert(self.size <= 3, "Triangulation bug, please report")

    cdef bool valid(self, Point p):
        return p != self.head and p != self.tail and self.is_convex(p)

    cdef void gen_mono_poly(self): 
        cdef Point p = self.head
        while(p != None):
            self.mono_poly.append(p)
            p = p.next

    cdef float angle(self, Point p):
        cdef Point a = p.next - p
        cdef Point b = p.prev - p
        return atan2(a.cross(b), a.dot(b))

    cdef float angle_sign(self):
        cdef Point a = self.head.next - self.head
        cdef Point b = self.tail - self.head
        return atan2(a.cross(b), a.dot(b)) >= 0

    cdef bool is_convex(self, Point p):
        if self.positive != (self.angle(p) >= 0): return False
        return True      
