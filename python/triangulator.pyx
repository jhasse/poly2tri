#
# Copyright (c) 2009 Mason Green & Tom Novelli
#
# This file is part of OpenMelee.
#
# OpenMelee is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# any later version.
#
# OpenMelee is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with OpenMelee.  If not, see <http://www.gnu.org/licenses/>.
#
from math import floor

###
### Based on Raimund Seidel's paper "A simple and fast incremental randomized
### algorithm for computing trapezoidal decompositions and for triangulating polygons"
### (Ported from poly2tri)

class Triangulator(object) {
  
    def __init__(points):

      # Convex polygon list
      self.polygons = []
      # Order and randomize the Edges
      self.EdgeList = initEdges()
      
      # Initialize trapezoidal map and query structure
      self.trapezoidalMap = new TrapezoidalMap
      self.bounding_box = trapezoidalMap.bounding_box(EdgeList)
      self.queryGraph = QueryGraph(Sink.init(bounding_box))
      self.xMonoPoly = []
      
      # The trapezoidal map 
      self.trapezoidMap = trapezoidalMap.map
      # Trapezoid decomposition list
      self.trapezoids = []
      
      self.process()
  
    // Build the trapezoidal map and query graph
    def process(self):

        i = 0
        while(i < len(EdgeList)):
          
            s = EdgeList(i)
            traps = queryGraph.followEdge(s)

            // Remove trapezoids from trapezoidal Map
            for j in range(len(traps)):
                trapezoidalMap.map -= traps(j)
          
            for j in range(len(traps)):
                t = traps(j)
                tList = []
                containsP = t.contains(s.p)
                containsQ = t.contains(s.q)
                if containsP and containsQ:
                  // Case 1
                  tList = trapezoidalMap.case1(t,s)
                  queryGraph.case1(t.sink, s, tList)
                elif containsP and !containsQ:
                  // Case 2
                  tList = trapezoidalMap.case2(t,s) 
                  queryGraph.case2(t.sink, s, tList)
                elif !containsP and !containsQ:
                  // Case 3
                  tList = trapezoidalMap.case3(t, s)
                  queryGraph.case3(t.sink, s, tList)
                else:
                  // Case 4
                  tList = trapezoidalMap.case4(t, s)
                  queryGraph.case4(t.sink, s, tList)
                
                // Add new trapezoids to map
                for k in range(len(tList)):
                    trapezoidalMap.map += tList[k]       
          
          trapezoidalMap.clear
          i += 1
        
        // Mark outside trapezoids
        for t in trapezoidalMap.map
            markOutside(t)
        
        // Collect interior trapezoids
        for t in trapezoidalMap.map
          if t.inside:
            trapezoids.append(t)
            t.addPoints()

        // Generate the triangles
        createMountains 
        
      }
  
    // Monotone polygons - these are monotone mountains
    def monoPolies(self): 
        polies = []
        for i in range(len(self.xMonoPoly)):
            polies.append(self.xMonoPoly(i).monoPoly)
        return polies
  
  
    // Build a list of x-monotone mountains
    private def createMountains {

        var i = 0
        while(i < EdgeList.size) {
          
          val s = EdgeList(i)
          
          if(s.mPoints.size > 0) {
            
             val mountain = new MonotoneMountain
             var k: List[Point] = None

             // Sorting is a perfromance hit. Literature says this can be accomplised in
             // linear time, although I don't see a way around using traditional methods
             // when using a randomized incremental algorithm
             if(s.mPoints.size < 10) 
               // Insertion sort is one of the fastest algorithms for sorting arrays containing 
               // fewer than ten elements, or for lists that are already mostly sorted.
               k = Util.insertSort((p1: Point, p2: Point) => p1 < p2)(s.mPoints).toList
             else 
               k = Util.msort((p1: Point, p2: Point) => p1 < p2)(s.mPoints.toList)
             
             val points = s.p :: k ::: List(s.q)
             
             var j = 0
             while(j < points.size) {
               mountain += points(j)
               j += 1
             }
             
             // Triangulate monotone mountain
             mountain process
             
             // Extract the triangles into a single list
             j = 0
             while(j < mountain.triangles.size) {
               polygons += mountain.triangles(j)
               j += 1
             }
             
             xMonoPoly += mountain
          }
          i += 1
        }   
    }
  
  // Mark the outside trapezoids surrounding the polygon
  private def markOutside(t: Trapezoid) {
	  if(t.top == bounding_box.top || t.bottom == bounding_box.bottom) {
	    t trimNeighbors
	  }
  }
  
  // Create Edges and connect end points; update edge event pointer
  private def initEdges: ArrayBuffer[Edge] = {
    var Edges = List[Edge]()
    for(i <- 0 until points.size-1) 
      Edges = new Edge(points(i), points(i+1)) :: Edges
    Edges =  new Edge(points.first, points.last) :: Edges
    orderEdges(Edges)
  }
  
  private def orderEdges(Edges: List[Edge]) = {
    
    // Ignore vertical Edges!
    val segs = new ArrayBuffer[Edge]
    for(s <- Edges) {
      val p = shearTransform(s.p)
      val q = shearTransform(s.q)
      // Point p must be to the left of point q
      if(p.x > q.x) {
        segs += new Edge(q, p)
      } else if(p.x < q.x) {
        segs += new Edge(p, q)
      }
    }
    // Randomized triangulation improves performance
    // See Seidel's paper, or O'Rourke's book, p. 57 
    Random.shuffle(segs)
    segs
  }
  
  // Prevents any two distinct endpoints from lying on a common vertical line, and avoiding
  // the degenerate case. See Mark de Berg et al, Chapter 6.3
  //val SHEER = 0.0001f
  def shearTransform(point: Point) = Point(point.x + 0.0001f * point.y, point.y)
 
}

// Doubly linked list
class MonotoneMountain {

	var tail, head: Point = None
	var size = 0

	private val convexPoints = new ArrayBuffer[Point]
    // Monotone mountain points
	val monoPoly = new ArrayBuffer[Point]
    // Triangles that constitute the mountain
	val triangles = new ArrayBuffer[Array[Point]]
	// Convex polygons that constitute the mountain
	val convexPolies = new ArrayBuffer[Array[Point]]
	// Used to track which side of the line we are on                                
	private var positive = false
	// Almost Pi!
	private val PI_SLOP = 3.1
 
	// Append a point to the list
	def +=(point: Point) {
	  size match {
	    case 0 => 
	      head = point
	      size += 1
      case 1 =>
        // Keep repeat points out of the list
        if(point ! head) {
	        tail = point
	        tail.prev = head
	        head.next = tail
	        size += 1
        }
      case _ =>
        // Keep repeat points out of the list
        if(point ! tail) {
	        tail.next = point
	        point.prev = tail
	        tail = point
	        size += 1
        }
	  }
	}

	// Remove a point from the list
	def remove(point: Point) {
		val next = point.next
		val prev = point.prev
		point.prev.next = next
		point.next.prev = prev
		size -= 1
	}
 
	// Partition a x-monotone mountain into triangles O(n)
	// See "Computational Geometry in C", 2nd edition, by Joseph O'Rourke, page 52
	def process {
	
	  // Establish the proper sign
	  positive = angleSign
	  // create monotone polygon - for dubug purposes
	  genMonoPoly
   
      // Initialize internal angles at each nonbase vertex
      // Link strictly convex vertices into a list, ignore reflex vertices
      var p = head.next
      while(p != tail) {
        val a = angle(p)
        // If the point is almost colinear with it's neighbor, remove it!
        if(a >= PI_SLOP || a <= -PI_SLOP)
        remove(p)
        else
        if(convex(p)) convexPoints += p
        p = p.next
      }
      
      triangulate

    }
 
	private def triangulate {
	  
      while(!convexPoints.isEmpty) {
	     
        val ear = convexPoints.remove(0)
        val a = ear.prev
        val b = ear
        val c = ear.next
        val triangle = Array(a, b, c)
      
        triangles += triangle
	     
        // Remove ear, update angles and convex list
        remove(ear)
        if(valid(a)) convexPoints += a
        if(valid(c)) convexPoints += c
      }
      assert(size <= 3, "Triangulation bug, please report")
   
	}

	private def valid(p: Point) = (p != head && p != tail && convex(p))
	  
	// Create the monotone polygon 
	private def genMonoPoly { 
      var p = head
	  while(p != None) {
        monoPoly += p
        p = p.next
	  }
	}
 
	private def angle(p: Point) = {
	  val a = (p.next - p)
	  val b = (p.prev - p)
	  Math.atan2(a cross b, a dot b)
	}
 
	private def angleSign = {
	  val a = (head.next - head)
	  val b = (tail - head)
	  (Math.atan2(a cross b, a dot b) >= 0)
	}
 
	// Determines if the inslide angle is convex or reflex
	private def convex(p: Point) = {
	  if(positive != (angle(p) >= 0)) false
      else true
  }

}

# Node for a Directed Acyclic graph (DAG)
class Node(object):

    def __init__(self, left, right):
        self.left = left
        self.right = right
        if left is not None: 
            left.parentList.append(self)
        if right is not None: 
            right.parentList.append(self)
        parentList = []
  
    def replace(self, node):
        for parent in node.parentList:
            if(parent.left == node):
                parent.left = self
            else:
                parent.right = self 
            parentList.append(parent)

# Directed Acyclic graph (DAG)
# See "Computational Geometry", 3rd edition, by Mark de Berg et al, Chapter 6.2
                           
class QueryGraph(var head: Node) {

  def locate(s: Edge) = head.locate(s).trapezoid
  
  def followEdge(s: Edge) = {
    
    val trapezoids = new ArrayBuffer[Trapezoid]
    trapezoids += locate(s)
    var j = 0
    while(s.q.x > trapezoids(j).rightPoint.x) {
      if(s > trapezoids(j).rightPoint) {
        trapezoids += trapezoids(j).upperRight
      } else {
        trapezoids += trapezoids(j).lowerRight
      }
      j += 1
    }
    trapezoids
  }
  
  def replace(sink: Sink, node: Node) {
    if(sink.parentList.size == 0) {
      head = node
    } else {
      node replace sink
    }
  }
  
  def case1(sink: Sink, s: Edge, tList: Array[Trapezoid]) {
    val yNode = new YNode(s, Sink.init(tList(1)), Sink.init(tList(2)))
    val qNode = new XNode(s.q, yNode, Sink.init(tList(3)))
	val pNode = new XNode(s.p, Sink.init(tList(0)), qNode)
    replace(sink, pNode)
  }
  
  def case2(sink: Sink, s: Edge, tList: Array[Trapezoid]) {
    val yNode = new YNode(s, Sink.init(tList(1)), Sink.init(tList(2)))
	val pNode = new XNode(s.p, Sink.init(tList(0)), yNode)
    replace(sink, pNode)
  }
  
  def case3(sink: Sink, s: Edge, tList: Array[Trapezoid]) {
    val yNode = new YNode(s, Sink.init(tList(0)), Sink.init(tList(1)))
    replace(sink, yNode)
  }
  
  def case4(sink: Sink, s: Edge, tList: Array[Trapezoid]) {
    val yNode = new YNode(s, Sink.init(tList(0)), Sink.init(tList(1)))
    val qNode = new XNode(s.q, yNode, Sink.init(tList(2)))
    replace(sink, qNode)
  }
  
}

class Sink(Node):

    def __new__(cls, trapezoid):
        if trapezoid.sink is not None:
            return trapezoid.sink
        else 
            return Sink(trapezoid)
            
    def __init__(self, trapezoid):
        Node.__init__(self, None, None)
        trapezoid.sink = self
  
    def locate(e): 
        return self

class TrapezoidalMap(object):

    map = {}
    margin = 50
    bcross = None
    tcross = None

    def clear(self):
        self.bcross = None
        self.tcross = None

    def case1(self, t, e):
        trapezoids = (None, None, None, None)
        trapezoids.append(Trapezoid(t.leftPoint, e.p, t.top, t.bottom))
        trapezoids.append(Trapezoid(e.p, e.q, t.top, e))
        trapezoids.append(Trapezoid(e.p, e.q, e, t.bottom))
        trapezoids.append(Trapezoid(e.q, t.rightPoint, t.top, t.bottom))
        trapezoids[0].updateLeft(t.upperLeft, t.lowerLeft)
        trapezoids[1].updateLeftRight(trapezoids[0], None, trapezoids[3], None)
        trapezoids[2].updateLeftRight(None, trapezoids[0], None, trapezoids[3])
        trapezoids[3].updateRight(t.upperRight, t.lowerRight)
        return trapezoids

    def case2(self, t, e):
        val rp = e.q if e.q.x == t.rightPoint.x else t.rightPoint
        trapezoids = (None, None, None)
        trapezoids.append(Trapezoid(t.leftPoint, e.p, t.top, t.bottom))
        trapezoids.append(Trapezoid(e.p, rp, t.top, e))
        trapezoids.append(Trapezoid(e.p, rp, e, t.bottom))
        trapezoids[0].updateLeft(t.upperLeft, t.lowerLeft)
        trapezoids[1].updateLeftRight(trapezoids[0], None, t.upperRight, None)
        trapezoids[2].updateLeftRight(None, trapezoids[0], None, t.lowerRight)
        self.bcross = t.bottom
        self.tcross = t.top
        e.above = trapezoids[1]
        e.below = trapezoids[2]
        return trapezoids
  
    def case3(self, t, e):
        lp = s.p if s.p.x == t.leftPoint.x  else t.leftPoint
        rp = s.q if s.q.x == t.rightPoint.x else t.rightPoint
        trapezoids = (None, None)
        if self.tcross is t.top:
            trapezoids[0] = t.upperLeft
            trapezoids[0].updateRight(t.upperRight, None)
            trapezoids[0].rightPoint = rp
        else:
            trapezoids[0] = Trapezoid(lp, rp, t.top, s)
            trapezoids[0].updateLeftRight(t.upperLeft, s.above, t.upperRight, None)
        if self.bcross is t.bottom:
            trapezoids[1] = t.lowerLeft
            trapezoids[1].updateRight(None, t.lowerRight)
            trapezoids[1].rightPoint = rp
        else:
            trapezoids[1] = Trapezoid(lp, rp, s, t.bottom)
            trapezoids[1].updateLeftRight(s.below, t.lowerLeft, None, t.lowerRight)
        self.bcross = t.bottom
        self.tcross = t.top
        s.above = trapezoids[0]
        s.below = trapezoids[1]
        return trapezoids

    def case4(self, t, e):
        lp = s.p if s.p.x == t.leftPoint.x else t.leftPoint
        trapezoids = (None, None, None)
        if self.tcross is t.top:
            trapezoids[0] = t.upperLeft
            trapezoids[0].rightPoint = s.q
        else:
            trapezoids[0] = Trapezoid(lp, s.q, t.top, s)
            trapezoids[0].updateLeft(t.upperLeft, s.above)
        if self.bcross is t.bottom:
            trapezoids[1] = t.lowerLeft
            trapezoids[1].rightPoint = s.q
        else:
            trapezoids[1] = Trapezoid(lp, s.q, s, t.bottom)
            trapezoids[1].updateLeft(s.below, t.lowerLeft)
        trapezoids[2] = Trapezoid(s.q, t.rightPoint, t.top, t.bottom)
        trapezoids[2].updateLeftRight(trapezoids[0], trapezoids[1], t.upperRight, t.lowerRight)
        
        return trapezoids
  
    def bounding_box(self, edges): 
        max = edges[0].p + margin
        min = edges[0].q - margin
        for s in edges:
            if s.p.x > max.x: max = Point(s.p.x + margin, max.y)
            if s.p.y > max.y: max = Point(max.x, s.p.y + margin)
            if s.q.x > max.x: max = Point(s.q.x+margin, max.y)
            if s.q.y > max.y: max = Point(max.x, s.q.y+margin)
            if s.p.x < min.x: min = Point(s.p.x-margin, min.y)
            if s.p.y < min.y: min = Point(min.x, s.p.y-margin)
            if s.q.x < min.x: min = Point(s.q.x-margin, min.y)
            if s.q.y < min.y: min = Point(min.x, s.q.y-margin)
        top = Edge(Point(min.x, max.y), Point(max.x, max.y))
        bottom = Edge(Point(min.x, min.y), Point(max.x, min.y))
        left = bottom.p
        right = top.q
        return Trapezoid(left, right, top, bottom)

class XNode(Node):

    def __init__(self, point, lchild, rchild):
        Node.__init__(self, lChild, rChild)
        self.point = point
        self.lchild = lchild
        self.rchild = rchils
    
    def locate(self, e): 
        if e.p.x >= self.point.x:
            return self.right.locate(e)
        else:         
            return self.left.locate(e)

class YNode(Node):

    def __init__(self, edge, lchild, rchild):
        Node.__init__(self, lChild, rChild)
        self.edge = edge
        self.lchild = lchild
        self.rchile = rchild
        
    def locate(self, e):
        if edge > e.p:
            return self.right.locate(e)
        elif edge < e.p:
            return self.left.locate(e)
        else:
          if e.slope < self.edge.slope:
            return self.right.locate(e)
          else:
            return self.left.locate(e)

cdef class Point(object):
  
    def __init__(self, x, y):
        self.x = x
        self.y = y
        next = None
        prev = None
        Edge = None
        edges = []

    cdef __sub__(self, Point p):
        return Point(self.x - p.x, self.y - p.y) 
    
    cdef __sub__(self, float f):
        return Point(self.x - f, self.y - f)
    
    cdef __add__(self, Point p):
        return Point(self.x + p.x, self.y + p.y)
    
    cdef __add__(self, float f):
        return Point(self.x + f, self.y + f)
    
    cdef __mul__(self, float f):
        return Point(self.x * f, self.y * f)
    
    cdef __div__(self, float a):
        return Point(self.x / a, self.y / a)
    
    cdef cross(self, Point p):
        return self.x * p.y - self.y * p.x
    
    cdef dot(self, Point p):
        return self.x * p.x + self.y * p.y
    
    cdef length(self):
        return math.sqrt(self.x * self.x + self.y * self.y)
        
    cdef normalize(self):
        return self / length  
        
    cdef __lt__(self, Point p):
        return self.x < p.x
  
    # Sort along y axis
    cdef >(p: Point):
        if y < p.y: 
          return True
        elif y > p.y:
          return False
        else {
          if x < p.x:
            return True
          else 
            return False
  
    cdef !(p: Point) = !(p.x == x && p.y == y)
    cdef clone = Point(x, y)
  

// Represents a simple polygon's edge
// TODO: Rename this class to Edge?
class Edge(object):

    def __init__(self, p, q):
        self.p = p
        self.q = q
        self.above, self.below = None
        mPoints = []
        self.slope = (q.y - p.y)/(q.x - p.x)
        self.b = p.y - (p.x * self.slope)
  
    def __gt__(self, point):
        return (floor(point.y) < floor(slope * point.x + b))
    def __lt__(self, point):
        return (floor(point.y) > floor(slope * point.x + b))

    def intersect(self, c, d):
        a = self.p
        b = self.q
        a1 = _signed_area(a, b, d)
        a2 = _signed_area(a, b, c)
        if  a1 != 0 and a2 != 0 and a1 * a2 < 0:
            a3 = _signed_area(c, d, a)
            a4 = a3 + a2 - a1
            if a3 * a4 < 0:
                t = a3 / (a3 - a4)
                return a + ((b - a) * t)
                
    def _signed_area(self, a, b, c):
        return (a.x - c.x) * (b.y - c.y) - (a.y - c.y) * (b.x - c.x)