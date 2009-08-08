/* Poly2Tri
 * Copyright (c) 2009, Mason Green
 * http://code.google.com/p/poly2tri/
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of Poly2Tri nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without specific
 *   prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.poly2tri.cdt

import scala.collection.mutable.ArrayBuffer

import shapes.{Segment, Point, Triangle}
import utils.Util

/**
 * Sweep-line, Constrained Delauney Triangulation (CDT)
 * See: Domiter, V. and Žalik, B.(2008)'Sweep-line algorithm for constrained Delaunay triangulation',
 *      International Journal of Geographical Information Science,22:4,449 — 462
 */
object CDT {
  
  // Inital triangle factor
  val ALPHA = 0.3f
  val SHEER = 0.00001f
  
  // Triangulate simple polygon
  def init(points: ArrayBuffer[Point]): CDT = {
    
    var xmax, xmin = shearTransform(points.first).x
    var ymax, ymin = shearTransform(points.first).y
    
    // Calculate bounds
    for(i <- 0 until points.size) { 
      points(i) = shearTransform(points(i))
      val p = points(i)
      if(p.x > xmax) xmax = p.x
      if(p.x < xmin) xmin = p.x
      if(p.y > ymax) ymax = p.y
      if(p.y < ymin) ymin = p.y
    }
    
    val deltaX = ALPHA * (xmax - xmin)
    val deltaY = ALPHA * (ymax - ymin)
    val p1 = Point(xmin - deltaX, ymin - deltaY)
    val p2 = Point(xmax + deltaX, p1.y)
    
    val segments = initSegments(points)
    val sortedPoints = pointSort(points)
    
    val noNeighbors = new Array[Triangle](3)
    val tPoints = Array(sortedPoints(0), p1, p2)
    val iTriangle = new Triangle(tPoints, noNeighbors)
    new CDT(sortedPoints, segments, iTriangle)
  }
  
    // Create segments and connect end points; update edge event pointer
  private def initSegments(points: ArrayBuffer[Point]): List[Segment] = {
    var segments = List[Segment]()
    for(i <- 0 until points.size-1) {
      segments = new Segment(points(i), points(i+1)) :: segments
      segments.first.updateEdge
    }
    segments =  new Segment(points.first, points.last) :: segments
    segments.first.updateEdge
    segments
  }
  
  // Insertion sort is one of the fastest algorithms for sorting arrays containing 
  // fewer than ten elements, or for lists that are already mostly sorted.
  // Merge sort: O(n log n)
  private def pointSort(points: ArrayBuffer[Point]): List[Point] = {
    if(points.size < 10) 
      Util.insertSort((p1: Point, p2: Point) => p1 > p2)(points).toList
    else
      Util.msort((p1: Point, p2: Point) => p1 > p2)(points.toList)
  }
  
  // Prevents any two distinct endpoints from lying on a common horizontal line, and avoiding
  // the degenerate case. See Mark de Berg et al, Chapter 6.3
  private def shearTransform(point: Point) = Point(point.x, point.y + point.x * SHEER)
  
}

class CDT(val points: List[Point], val segments: List[Segment], iTriangle: Triangle) {
  
  // Triangle list
  def triangles = mesh.triangles
  def triangleMesh = mesh.map
  def debugTriangles = mesh.debug
  
  // The triangle mesh
  private val mesh = new Mesh(iTriangle)
  // Advancing front
  private val aFront = new AFront(iTriangle)
  
  private val PI_2 = Math.Pi/2
  private val PI_34 = Math.Pi*3/4
  
  // Triangle used to clean interior
  var cleanTri: Triangle = null
  
  // Sweep points; build mesh
  sweep
  // Finalize triangulation
  finalization
  
  // Implement sweep-line 
  private def sweep {
    for(i <- 1 until points.size) {
      val point = points(i)
      // Process Point event
      var triangle: Triangle = null
      try {
        triangle = pointEvent(point)
      } catch {
        case e: Exception => 
          println("Offending triangle = " + i)
      }
      // Process edge events
      point.edges.foreach(e => edgeEvent(e, triangle))
      if(i == 7) {cleanTri = triangle; mesh.debug += cleanTri}
    }
  }  
  
  // Final step in the sweep-line CDT algo
  // Clean exterior triangles
  private def finalization {
    mesh.map.foreach(m => m.markEdges)
    mesh clean cleanTri
    //mesh.map.foreach(m => m.edges.foreach(e => if(e) mesh.debug += m))
  }
  
  // Point event
  private def pointEvent(point: Point): Triangle = {
    
    val node = aFront.locate(point)
    
    // Projected point coincides with existing point; create two triangles
    if(point.x == node.point.x && node.prev != null) {
        
    	val rPts = Array(point, node.point, node.next.point)
        val rNeighbors = Array(node.triangle, null, null)
        val rTriangle = new Triangle(rPts, rNeighbors)
        
        val lPts = Array(point, node.prev.point, node.point)
        val lNeighbors = Array(node.prev.triangle, rTriangle, null)
        val lTriangle = new Triangle(lPts, lNeighbors)
        
        rTriangle.neighbors(2) = lTriangle
        mesh.map += lTriangle
        mesh.map += rTriangle
        
        // TODO: check to see of legalization is necessary here
        
        // Update neighbors
        node.triangle.markNeighbor(rTriangle.points(1), rTriangle.points(2), rTriangle, mesh.debug)
        node.prev.triangle.markNeighbor(lTriangle.points(1), lTriangle.points(2), lTriangle, mesh.debug)
        
        // Update advancing front
	    val newNode = aFront.insert(point, rTriangle, node)
	    node.prev.next = newNode
	    newNode.prev = node.prev
	    node.prev.triangle = lTriangle
       
        // Fill in adjacent triangles if required
	    scanAFront(newNode)
	    newNode.triangle
        
    } else {
      
        // Projected point hits advancing front; create new triangle 
	    val cwPoint = node.next.point
	    val ccwPoint = node.point
	    val nTri = node.triangle
	    
	    val pts = Array(point, ccwPoint,  cwPoint)
	    val neighbors = Array(nTri, null, null)
	    val triangle = new Triangle(pts, neighbors)
	    mesh.map += triangle
	    
        // Legalize
	    val legal = legalization(triangle, nTri)
        var newNode: Node = null
        
        // Update advancing front
        if(legal) { 
          newNode = aFront.insert(point, triangle, node)
          // Update neighbors
          nTri.markNeighbor(cwPoint, ccwPoint, triangle, mesh.debug)
        } else {
          newNode = new Node(triangle.points(1), triangle)
          val rNode = node.next
          rNode.prev = newNode
          newNode.next = rNode
          node.next = newNode
          newNode.prev = node
        }
        
        // Fill in adjacent triangles if required
	    scanAFront(newNode)
	    newNode.triangle
	}
  }
  
  // EdgeEvent
  private def edgeEvent(edge: Segment, triangle: Triangle) { 
    
    // STEP 1: Locate the first intersected triangle
    val firstTriangle = triangle.locateFirst(edge)
    
    val contains = if(firstTriangle != null) firstTriangle.contains(edge) else false
    
    // STEP 2: Remove intersected triangles
    if(firstTriangle != null && !contains) {
      
       // Collect intersected triangles
       val tList = new ArrayBuffer[Triangle]
       tList += firstTriangle
       
       while(tList.last != null && !tList.last.contains(edge.p))
         tList += tList.last.findNeighbor(edge.p - edge.q)
       
       // TODO: fix tList.last == null bug!
       // Not sure why this happens in bird & nazca monkey demo...
       if(tList.last == null)
         tList -= tList.last
       
        // Remove old triangles
        tList.foreach(t => mesh.map -= t)
      
		val lPoints = new ArrayBuffer[Point]
		val rPoints = new ArrayBuffer[Point]
		  
		val ahead = (edge.p.x > edge.q.x)
		val point1 = if(ahead) edge.q else edge.p
		val point2 = if(ahead) edge.p else edge.q
		  
        // Collect points left and right of edge
		  tList.foreach(t => {
		    t.points.foreach(p => {
		      if(p != edge.q && p != edge.p) {
		        if(t.orient(point1, point2, p) >= 0 ) {
		          // Keep duplicate points out
                  if(!lPoints.contains(p)) {
		            lPoints += p
                  }
		        } else { 
		          // Keep duplicate points out
                  if(!rPoints.contains(p))
		            rPoints += p
                }
               }
		    })
		  })
      
      // STEP 3: Triangulate empty areas.
      val T1 = new ArrayBuffer[Triangle]
      triangulate(lPoints.toArray, List(point1, point2), T1)
      val T2 = new ArrayBuffer[Triangle]
      triangulate(rPoints.toArray, List(point1, point2), T2)
      
       // Mark constrained edges
       val dEdge = new Segment(point1, point2)
       T1.first mark(point1, point2)
       T2.first mark(point1, point2)
       
       //TODO update neighbor pointers
       
    } else if(firstTriangle == null) {
      
      // NOTE: So far this only works for single triangles
      // No triangles are intersected by the edge; edge must lie outside the mesh
      // Apply constraint; traverse the AFront, and build triangles
      
      val ahead = (edge.p.x > edge.q.x)
      val point1 = if(ahead) edge.q else edge.p
      val point2 = if(ahead) edge.p else edge.q
      
      val points = new ArrayBuffer[Point]
      val triangles = new ArrayBuffer[Triangle]
      
      var node = aFront.locate(point1)
      val first = node
      triangles += first.triangle
      
      node = node.next
      
	  while(node.point != point2) {
		points += node.point
        triangles += node.triangle
		node = node.next
	  }
                                  
      val endPoints = if(ahead) List(point2, point1) else List(point1, point2)
   
      // STEP 3: Triangulate empty areas.
      val T = new ArrayBuffer[Triangle]
      triangulate(points.toArray, endPoints, T)
      
      // Update advancing front 
      aFront -= (first, node.prev, T.first)
     
      // Update neigbor pointers
      // Inneficient, but it works well...
      for(i <- triangles) 
        for(j <- T) 
          i.markNeighbor(j)
      
      for(i <- 0 until T.size)
        for(j <- i+1 until T.size) 
          T(i).markNeighbor(T(j))
          
       // Mark constrained edge
      T.first mark(edge.p, edge.q)
      
    } else { 
      // Mark constrained edge
      firstTriangle mark(edge.p, edge.q)
    }
  }
  
  // Marc Vigo Anglada's triangulate pseudo-polygon algo 
  private def triangulate(P: Array[Point], ab: List[Point], T: ArrayBuffer[Triangle]) {
    
    val a = ab.first
    val b = ab.last
    
    if(P.size > 1) {
      var c = P.first
      var i = 0
      for(j <- 1 until P.size) {
        if(illegal(a, c, b, P(j))) {
          c = P(j)
          i = j
        } 
      }
      val PE = P.slice(0, i)
      val PD = P.slice(i, P.size-1)
      triangulate(PE, List(a, c), T)
      triangulate(PD, List(c, b), T)
    } 
    
    if(!P.isEmpty) {
      val points = Array(a, P.first, b)
      val neighbors = new Array[Triangle](3)
      T += new Triangle(points, neighbors)
      mesh.map += T.last
    }
  }

  // Scan left and right along AFront to fill holes
  private def scanAFront(n: Node) {
    
    var node = n.next
    // Update right
    if(node.next != null) {
      var angle = 0.0
      do {
        angle = fill(node)
        node = node.next
      } while(angle <= PI_2 && node.next != null) 
    }
    
    node = n.prev
    // Update left
    if(node.prev != null) {
      var angle = 0.0
      do {
	    angle = fill(node)
        node = node.prev
      } while(angle <= PI_2 && node.prev != null)
    }
  }
  
  // Fill empty space with a triangle
  private def fill(node: Node): Double = {
	  val a = (node.prev.point - node.point)
	  val b = (node.next.point - node.point)
	  val angle = Math.abs(Math.atan2(a cross b, a dot b))
	  if(angle <= PI_2) {
	    val points = Array(node.prev.point, node.point, node.next.point)
	    val neighbors = Array(node.triangle, null, node.prev.triangle)
	    val triangle = new Triangle(points, neighbors)
        // Update neighbor pointers
        node.prev.triangle.markNeighbor(triangle.points(0), triangle.points(1), triangle, mesh.debug)
        node.triangle.markNeighbor(triangle.points(1), triangle.points(2), triangle, mesh.debug)
	    mesh.map += triangle
	    aFront -= (node.prev, node, triangle)
	  }
      angle
  }
  
  // Do edges need to be swapped? Robust CircumCircle test
  // See section 3.7 from "Triangulations and Applications" by O. Hjelle & M. Deahlen
  private def illegal(p1: Point, p2: Point, p3: Point, p4:Point): Boolean = {
    
	  val v1 = p3 - p2
      val v2 = p1 - p2
      val v3 = p1 - p4
      val v4 = p3 - p4
      
      val cosA = v1 dot v2
      val cosB = v3 dot v4
      
      if(cosA < 0 && cosB < 0)
        return true 
      else if(cosA > 0 && cosB > 0)
        return false
      
      val sinA = v1 cross v2
      val sinB = v3 cross v4

      // Small negative number to prevent swapping
      // in nearly neutral cases
      if((cosA*sinB + sinA*cosB) < -0.01f) 
        true
      else
        false
  }
  
  // Ensure adjacent triangles are legal
  // If illegal, flip edges and update triangle's pointers
  private def legalization(t1: Triangle, t2: Triangle): Boolean = {
    
    val oPoint = t2 oppositePoint t1
    
    if(illegal(t1.points(1), oPoint, t1.points(2), t1.points(0))) {
       
       // Prevent creation of collinear traingles
	   val c1 = t1.collinear(oPoint)
	   val c2 = t2.collinear(oPoint, t1.points(0))	   
    
       if(!c1 && !c2) {   
        
	        // Update neighbor pointers
	        val ccwNeighbor = t2.neighborCCW(oPoint)
	       
	        if(ccwNeighbor != null) {
	          ccwNeighbor.markNeighbor(oPoint, t1.points(2), t1, mesh.debug)
	          t1.neighbors(1) = ccwNeighbor
	        }
	        
	        t2.rotateNeighborsCW(oPoint, t1)
	         
	        t1.neighbors(0) = t2
	        t1.neighbors(2) = null
	        
	        // Flip edges and rotate everything clockwise
	        val point = t1.points(0)
		    t1.legalize(oPoint) 
		    t2.legalize(oPoint, point)
	        return false
       } 
    } else {
      true
    }
    true
  }  
}
