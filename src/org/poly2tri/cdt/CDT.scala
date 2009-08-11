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
  
  var clearPoint = 0
  
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
    
    val tPoints = Array(sortedPoints(0), p1, p2)
    val iTriangle = new Triangle(tPoints)
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
      var triangle = pointEvent(point)
      // Process edge events
      if(triangle != null)
        point.edges.foreach(e => triangle = edgeEvent(e, triangle))
      if(i == CDT.clearPoint) {cleanTri = triangle; mesh.debug += cleanTri}
    }
    
  }  
  
  // Final step in the sweep-line CDT algo
  // Clean exterior triangles
  private def finalization {
    
    mesh.map.foreach(m => m.markEdges)
    mesh clean cleanTri
    
  }
  
  // Point event
  private def pointEvent(point: Point): Triangle = {
    
    val node = aFront.locate(point)
    
    // Projected point hits advancing front; create new triangle 
    val pts = Array(point, node.point, node.next.point)
    val triangle = new Triangle(pts)
      
    mesh.map += triangle
    
    // Legalize
    val newNode = legalization(triangle, node)
    // Fill in adjacent triangles if required
    scanAFront(newNode)
    newNode.triangle

  }
  
  // EdgeEvent
  private def edgeEvent(edge: Segment, triangle: Triangle): Triangle = { 
    
    // Locate the first intersected triangle
    val firstTriangle = triangle.locateFirst(edge)
    
    // Remove intersected triangles
    if(firstTriangle != null && !firstTriangle.contains(edge)) {
       println("collect")
       // Collect intersected triangles
       val tList = new ArrayBuffer[Triangle]
       tList += firstTriangle
       
       while(tList.last != null && !tList.last.contains(edge.p))
         tList += tList.last.findNeighbor(edge.p - edge.q)
       
       // TODO: fix tList.last == null bug!
       // Not sure why this happens in bird & nazca monkey demo...
       if(tList.last == null) tList -= tList.last
       
       // Neighbor triangles
       val nTriangles = new ArrayBuffer[Triangle]
       
        // Remove old triangles; collect neighbor triangles
        tList.foreach(t => {
          t.neighbors.foreach(n => if(n != null && !tList.contains(n)) nTriangles += n)
          mesh.map -= t
        })
      
		val lPoints = new ArrayBuffer[Point]
		val rPoints = new ArrayBuffer[Point]
		   
        // Collect points left and right of edge
		tList.foreach(t => {
		  t.points.foreach(p => {
		    if(p != edge.q && p != edge.p) {
		      if(t.orient(edge.q, edge.p, p) > 0 ) {
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
      
      // Triangulate empty areas.
      val T1 = new ArrayBuffer[Triangle]
      triangulate(lPoints.toArray, List(edge.q, edge.p), T1)
      val T2 = new ArrayBuffer[Triangle]
      triangulate(rPoints.toArray, List(edge.q, edge.p), T2)
      
      // Update advancing front
  
      val ahead = (edge.p.x > edge.q.x)
	  val point1 = if(ahead) edge.q else edge.p 
	  val point2 = if(ahead) edge.p else edge.q 
  
      var first: Triangle = null
      val sNode = aFront.locatePoint(point1)  
      val eNode = aFront.locatePoint(point2)
      
      if(sNode != null && eNode != null) {
        first = aFront.constrainedEdge(sNode, eNode, T1, T2)
      } else {
        val qNode = if(sNode == null) eNode else sNode
        first = aFront.constrainedEdge(qNode, tList, T1, T2)
      }
     
      // Update neighbors
      edgeNeighbors(nTriangles, T1)
      edgeNeighbors(nTriangles, T2)
      
       // Mark constrained edges
       T1.first.mark(point1, point2)
       T2.first.mark(point1, point2)
       
       // Double check returning T2.first vs T1.first      
       first

    } else if(firstTriangle == null) {
      println("intersect")
      // No triangles are intersected by the edge; edge must lie outside the mesh
      // Apply constraint; traverse the AFront, and build triangles
      
      val ahead = (edge.p.x > edge.q.x)
      
      val point1 = if(ahead) edge.q else edge.p
      val point2 = if(ahead) edge.p else edge.q
      
      val points = new ArrayBuffer[Point]
      // Neighbor triangles
      val nTriangles = new ArrayBuffer[Triangle]
      
      var node = aFront.locate(point1)
      
      val first = node
      nTriangles += node.triangle
      
      node = node.next
      
	  while(node.point != point2) {
		points += node.point
        nTriangles += node.triangle
		node = node.next
	  }
                                  
      val endPoints = if(ahead) List(point2, point1) else List(point1, point2)
   
      // Triangulate empty areas.
      val T = new ArrayBuffer[Triangle]
      triangulate(points.toArray, endPoints, T)
      
      // Update advancing front 
      aFront link (first, node, T.first)
      
      // Update neighbors
      edgeNeighbors(nTriangles, T)
      
       // Mark constrained edge
      T.first mark(edge.p, edge.q)
      
      // Return original triangle
      triangle
      
    } else { 
      println("contains")
      // Mark constrained edge
      firstTriangle mark(edge.p, edge.q)
      triangle
    }
    
  }
  
  // Update neigbor pointers for edge event
  // Inneficient, but it works well...
  def edgeNeighbors(nTriangles: ArrayBuffer[Triangle], T: ArrayBuffer[Triangle]) {
    
    for(t1 <- nTriangles) 
      for(t2 <- T) 
        t1.markNeighbor(t2) 
                                                                                
    for(i <- 0 until T.size) 
      for(j <- i+1 until T.size) 
        T(i).markNeighbor(T(j))
    
  }
  
  
  // Marc Vigo Anglada's triangulate pseudo-polygon algo 
  private def triangulate(P: Array[Point], ab: List[Point], T: ArrayBuffer[Triangle]) {
    
    val a = ab.first
    val b = ab.last
    var i = 0
    
    if(P.size > 1) {
      var c = P.first
      for(j <- 1 until P.size) {
        if(illegal(a, P(j), b, c)) {
          c = P(j)
          i = j
        } 
      }
      val PE = P.slice(0, i)
      val PD = P.slice(i+1, P.size)
      triangulate(PE, List(a, c), T)
      triangulate(PD, List(c, b), T)
    } 
    
    if(!P.isEmpty) {
      val left = Util.orient(a, b, P(i)) < 0
      val pB = if(left) P(i) else b
      val pC = if(left) b else P(i)
      val points = Array(a, pB, pC)
      T += new Triangle(points)
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
	    val triangle = new Triangle(points)
        // Update neighbor pointers
        node.prev.triangle.markNeighbor(triangle)
        node.triangle.markNeighbor(triangle)
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
      
      if(cosA > 0 && cosB > 0)
        return false
      
      val sinA = v1 cross v2
      val sinB = v3 cross v4

      if((cosA*sinB + sinA*cosB) < 0f) 
        true
      else
        false
  }
  
  // Ensure adjacent triangles are legal
  // If illegal, flip edges and update triangle's pointers
  private def legalization(t1: Triangle, node: Node): Node = {
    
    val t2 = node.triangle
    
    val point = t1.points(0)
    val oPoint = t2 oppositePoint t1
    
    if(illegal(t1.points(1), oPoint, t1.points(2), t1.points(0))) {
      
        println("legalize")
        // Flip edge and rotate everything clockwise
	    t1.legalize(oPoint) 
	    t2.legalize(oPoint, point)
        
        // Copy old neighbors
	    val neighbors = List(t2.neighbors(0), t2.neighbors(1), t2.neighbors(2))
        // Clear old neighbors
        t2.clearNeighbors
        // Update new neighbors
	    for(n <- neighbors) {
	      if(n != null) {
	        t2.markNeighbor(n)
            t1.markNeighbor(n)
	      }
	    }
        t2.markNeighbor(t1)
        
        // Update advancing front
        aFront.insertLegalized(t1.points(1), t1, node)
        
    } else {
      
      // Update neighbor
      t2.markNeighbor(t1) 
      // Update advancing front
      aFront.insert(point, t1, node)

    }

  }  
  
}
