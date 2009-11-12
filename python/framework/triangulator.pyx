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
from math import floor

###
### Based on Raimund Seidel'e paper "A simple and fast incremental randomized
### algorithm for computing trapezoidal decompositions and for triangulating polygons"
### (Ported from poly2tri)

cdef extern from 'math.h':
    double cos(double)
    double sin(double)
    double sqrt(double)

cdef list merge_sort(list l):
    cdef list lleft, lright
    cdef int p1, p2, p
    if len(l)>1 :
        lleft = merge_sort(l[:len(l)/2])
        lright = merge_sort(l[len(l)/2:])
        #do merge here
        p1,p2,p = 0,0,0
        while p1<len(lleft) and p2<len(lright):
            if lleft[p1][0] < lright[p2][0]:
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
        Point lpoint, rpoint
        Edge top, bottom
        Trapezoid upper_left, lower_left
        Trapezoid upper_right, lower_right
        bool inside
        
    sink = None
    
    def __init__(self, Point lpoint, Point rpoint, Edge top, Edge bottom):
        self.lpoint = lpoint
        self.rpoint = rpoint
        self.top = top
        self.bottom = bottom
        self.upper_left = None
        self.upper_right = None
        self.lower_left = None
        self.lower_right = None
        self.inside = True
    
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
        return (point.x > self.lpoint.x and point.x < self.rpoint.x and 
                self.top.is_above(point) and self.bottom.is_below(point))
  
    def vertices(self):
        cdef list verts = []
        verts.append(line_intersect(self.top, self.lpoint.x))
        verts.append(line_intersect(self.bottom, self.lpoint.x))
        verts.append(line_intersect(self.bottom, self.rpoint.x))
        verts.append(line_intersect(self.top, self.rpoint.x))
        return verts
  
    def add_points(self):
        if self.lpoint != self.bottom.p: 
            self.bottom.mpoints.append(self.lpoint.clone)
        if self.rpoint != self.bottom.q: 
            self.bottom.mpoints.append(self.rpoint.clone)
        if self.lpoint != self.top.p: 
            self.top.mpoints.append(self.lpoint.clone)
        if self.rpoint != self.top.q: 
            self.top.mpoints.append(self.rpoint.clone)

class TrapezoidalMap:

    map = {}
    margin = 50
    bcross = None
    tcross = None

    def clear(self):
        self.bcross = None
        self.tcross = None

    def case1(self, t, e):
        trapezoids = [None, None, None, None]
        trapezoids.append(Trapezoid(t.lpoint, e.p, t.top, t.bottom))
        trapezoids.append(Trapezoid(e.p, e.q, t.top, e))
        trapezoids.append(Trapezoid(e.p, e.q, e, t.bottom))
        trapezoids.append(Trapezoid(e.q, t.rpoint, t.top, t.bottom))
        trapezoids[0].update_left(t.upper_left, t.lower_left)
        trapezoids[1].update_left_right(trapezoids[0], None, trapezoids[3], None)
        trapezoids[2].update_left_right(None, trapezoids[0], None, trapezoids[3])
        trapezoids[3].update_right(t.upper_right, t.lower_right)
        return trapezoids

    def case2(self, t, e):
        rp = e.q if e.q.x == t.rpoint.x else t.rpoint
        trapezoids = [None, None, None]
        trapezoids.append(Trapezoid(t.lpoint, e.p, t.top, t.bottom))
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
        lp = e.p if e.p.x == t.lpoint.x  else t.lpoint
        rp = e.q if e.q.x == t.rpoint.x else t.rpoint
        trapezoids = [None, None]
        if self.tcross is t.top:
            trapezoids[0] = t.upper_left
            trapezoids[0].update_right(t.upper_right, None)
            trapezoids[0].rpoint = rp
        else:
            trapezoids[0] = Trapezoid(lp, rp, t.top, e)
            trapezoids[0].update_left_right(t.upper_left, e.above, t.upper_right, None)
        if self.bcross is t.bottom:
            trapezoids[1] = t.lower_left
            trapezoids[1].update_right(None, t.lower_right)
            trapezoids[1].rpoint = rp
        else:
            trapezoids[1] = Trapezoid(lp, rp, e, t.bottom)
            trapezoids[1].update_left_right(e.below, t.lower_left, None, t.lower_right)
        self.bcross = t.bottom
        self.tcross = t.top
        e.above = trapezoids[0]
        e.below = trapezoids[1]
        return trapezoids

    def case4(self, t, e):
        lp = e.p if e.p.x == t.lpoint.x else t.lpoint
        trapezoids = [None, None, None]
        if self.tcross is t.top:
            trapezoids[0] = t.upper_left
            trapezoids[0].rpoint = e.q
        else:
            trapezoids[0] = Trapezoid(lp, e.q, t.top, e)
            trapezoids[0].update_left(t.upper_left, e.above)
        if self.bcross is t.bottom:
            trapezoids[1] = t.lower_left
            trapezoids[1].rpoint = e.q
        else:
            trapezoids[1] = Trapezoid(lp, e.q, e, t.bottom)
            trapezoids[1].update_left(e.below, t.lower_left)
        trapezoids[2] = Trapezoid(e.q, t.rpoint, t.top, t.bottom)
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
        if left is not None: 
            left.parent_list.append(self)
        if right is not None: 
            right.parent_list.append(self)
  
    def replace(self, node):
        for parent in node.parent_list:
            if parent.left is node:
                parent.left = self
            else:
                parent.right = self 
            self.parent_list.append(parent)

class Sink(Node):

    def __new__(cls, trapezoid):
        if trapezoid.sink is not None:
            return trapezoid.sink
        else:
            return Sink(trapezoid)
            
    def __init__(self, trapezoid):
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
        else:         
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
          else:
            return self.left.locate(e)
            
class QueryGraph:

    head = None
    
    def __init__(self, head):
        self.head = head
        
    def locate(self, e):
        return self.head.locate(e).trapezoid
  
    def follow_segment(self, e):
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
        
