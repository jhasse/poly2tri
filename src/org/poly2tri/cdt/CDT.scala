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
    val p2 = Point(xmax + deltaX, ymin - deltaY)
    
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
  def triangles = mesh.map
  def debugTriangles = mesh.debug
  
  // The triangle mesh
  private val mesh = new Mesh(iTriangle)
  // Advancing front
  private val aFront = new AFront(iTriangle)
  
  private val PI_2 = Math.Pi/2
  
  // Sweep points; build mesh
  sweep
  // Finalize triangulation
  finalization
  
  // Implement sweep-line 
  private def sweep {
    for(i <- 1 until points.size) {
      val point = points(i)
      // Process Point event
      val triangle = pointEvent(point)
      // Process edge events
      point.edges.foreach(e => edgeEvent(e, triangle))
    }
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
        
        // Legalize new triangles
        val rLegal = legalization(rTriangle, rTriangle.neighbors(0))
        val lLegal = legalization(lTriangle, lTriangle.neighbors(0))
        var scanNode: Node = null
        
        // Update advancing front
        if(rLegal) { 
          // Update neighbors
	      node.triangle.updateNeighbors(rTriangle.points(1), rTriangle.points(2), rTriangle, mesh.debug)
          scanNode = aFront.insert(point, rTriangle, node)
        } else {
          scanNode = new Node(rTriangle.points(1), rTriangle)
          scanNode.next = node.next
          node.next = scanNode
          scanNode.prev = node
        }
        
        // Update neighbor pointers
        if(lLegal) {
          lTriangle.neighbors(0).updateNeighbors(lTriangle.points(1), lTriangle.points(2), lTriangle, mesh.debug)
          node.prev.next = scanNode
          scanNode.prev = node.prev
          node.prev.triangle = lTriangle
        } else {
        }       
       
        // Fill in adjacent triangles if required
	    scanAFront(scanNode)
	    scanNode.triangle
        
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
        var scanNode: Node = null
        
        // Update advancing front
        if(legal) { 
          // Update neighbors
	      nTri.updateNeighbors(triangle.points(1), triangle.points(2), triangle, mesh.debug)
          scanNode = aFront.insert(point, triangle, node)
        } else {
          scanNode = new Node(triangle.points(1), triangle)
          val rNode = node.next
          rNode.prev = scanNode
          scanNode.next = rNode
          node.next = scanNode
          scanNode.prev = node
        }
        
        // Fill in adjacent triangles if required
	    scanAFront(scanNode)
	    scanNode.triangle
	}
  }
  
  // EdgeEvent
  private def edgeEvent(edge: Segment, triangle: Triangle) { 
    
    // STEP 1: Locate the first intersected triangle
    val firstTriangle = triangle.locateFirst(edge)
    
    // STEP 2: Remove intersected triangles
    if(firstTriangle != null && !firstTriangle.contains(edge)) {
      
       // Collect intersected triangles
       val triangles = new ArrayBuffer[Triangle]
       triangles += firstTriangle
       
       while(triangles.last != null && !triangles.last.contains(edge.p))
         triangles += triangles.last.findNeighbor(edge.p - edge.q)
       
       // TODO: fix triangles.last == null bug!
       // This happens in the bird & nazca monkey demo...
       if(triangles.last == null)
         triangles -= triangles.last
       
        // Remove old triangles
        triangles.foreach(t => mesh.map -= t)
      
		val lPoints = new ArrayBuffer[Point]
		val rPoints = new ArrayBuffer[Point]
		  
		val ahead = (edge.p.x > edge.q.x)
		val point1 = if(ahead) edge.q else edge.p
		val point2 = if(ahead) edge.p else edge.q
		  
        // Collect points left and right of edge
		  triangles.foreach(t => {
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
      
      // TODO: Update Delauney Edge Pointers
      
    } else if(firstTriangle == null) {
      
      // No triangles are intersected by the edge; edge must lie outside the mesh
      // Apply constraint; traverse the AFront, and build triangles
      
      val ahead = (edge.p.x > edge.q.x)
      val point1 = if(ahead) edge.q else edge.p
      val point2 = if(ahead) edge.p else edge.q
      
      val points = new ArrayBuffer[Point]
      var node = aFront.locate(point1)
      val first = node
      
	  while(node.point != point2) {
		points += node.point
		node = node.next
	  }
      
      // STEP 3: Triangulate empty areas.
      val T = new ArrayBuffer[Triangle]
      triangulate(points.toArray, List(point1, point2), T)
      
      // Update advancing front 
      first.triangle = T.first
      first.next = node
      node.prev = first
      
      // TODO: Update Delauney Edge Pointers
      
    } else if(firstTriangle.contains(edge)) {
      // TODO: Update Delauney Edge Pointers
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
      // TODO: Correctly update neighbor pointers?
      // Not updating seems to work with simple polygons...
      val neighbors = new Array[Triangle](3)
      T += new Triangle(points, neighbors)
      mesh.map += T.last
    }
  }

  // Scan left and right along AFront to fill holes
  def scanAFront(n: Node) {
    
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
  def fill(node: Node): Double = {
     
	  val a = (node.prev.point - node.point)
	  val b = (node.next.point - node.point)
	  val angle = Math.abs(Math.atan2(a cross b, a dot b))
	  if(angle <= PI_2) {
	    val points = Array(node.prev.point, node.point, node.next.point)
	    val neighbors = Array(node.triangle, null, node.prev.triangle)
	    val triangle = new Triangle(points, neighbors)
        // Update neighbor pointers
        node.prev.triangle.updateNeighbors(triangle.points(0), triangle.points(1), triangle, mesh.debug)
        node.triangle.updateNeighbors(triangle.points(1), triangle.points(2), triangle, mesh.debug)
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
      
      // Some small number
      if(cosA*sinB + sinA*cosB < -0.01f)
        true
      else
        false
  }
  
  // Ensure adjacent triangles are legal
  // If illegal, flip edges and update triangle's pointers
  private def legalization(t1: Triangle, t2: Triangle): Boolean = {
    
    val oPoint = t2 oppositePoint t1
    
    if(illegal(t1.points(1), oPoint, t1.points(2), t1.points(0))) {
	    // Flip edges and rotate everything clockwise
        val point = t1.points(0)
	    t1.legalize(oPoint) 
	    t2.legalize(oPoint, point)
	    
	    // TODO: Make sure this is correct
        val cwNeighbor = t2.neighborCW(oPoint)
        val ccwNeighbor = t2.neighborCCW(oPoint)
        
        t1.neighbors(0) = t2
	    t1.neighbors(1) = cwNeighbor
        t1.neighbors(2) = null
        
	    if(t2.points(0) == oPoint) {
	      t2.neighbors(0) = null
          t2.neighbors(1) = ccwNeighbor
          t2.neighbors(2) = t1
	    } else {
	      t2.neighbors(0) = ccwNeighbor
          t2.neighbors(1) = t1
          t2.neighbors(2) = null
	    }
	    false
    } else {
      true
    }
  }
 
  // Final step in the sweep-line CDT algo
  private def finalization {
    
  }
  
}
