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
package org.poly2tri.shapes

import scala.collection.mutable.ArrayBuffer

import utils.Util

// Triangle-based data structures are know to have better performance than quad-edge structures
// See: J. Shewchuk, "Triangle: Engineering a 2D Quality Mesh Generator and Delaunay Triangulator"
//      "Triangulations in CGAL"
class Triangle(val points: Array[Point]) {

  // Neighbor pointers
  var neighbors = new Array[Triangle](3)
  // Flags to determine if an edge is the final Delauney edge
  val edges = Array(false, false, false)
  // Finalization flag. Set true if legalized or edge constrained
  var finalized = false
  // Has this triangle been marked as an interoir triangle?
  var interior = false
  
  def contains(p: Point): Boolean = (p == points(0) || p == points(1) || p == points(2))
  def contains(e: Segment): Boolean = (contains(e.p) && contains(e.q))
  def contains(p: Point, q: Point): Boolean = (contains(p) && contains(q))
  
  // Update neighbor pointers
  private def markNeighbor(p1: Point, p2: Point, t: Triangle) {    
    
    if((p1 == points(2) && p2 == points(1)) || (p1 == points(1) && p2 == points(2))) 
      neighbors(0) = t 
    else if((p1 == points(0) && p2 == points(2)) || (p1 == points(2) && p2 == points(0)))
      neighbors(1) = t
    else if((p1 == points(0) && p2 == points(1)) || (p1 == points(1) && p2 == points(0)))
      neighbors(2) = t
    else {
      throw new Exception("Neighbor error, please report!")
    }
  }
  
  /* Exhaustive search to update neighbor pointers */
  def markNeighbor(t: Triangle) {
    
    if (t.contains(points(1), points(2))) {
      neighbors(0) = t
      t.markNeighbor(points(1), points(2), this)
    } else if(t.contains(points(0), points(2))) {
      neighbors(1) = t
      t.markNeighbor(points(0), points(2), this)
    } else if (t.contains(points(0), points(1))) {
      neighbors(2) = t
      t.markNeighbor(points(0), points(1), this)
    }
    
  }
  
  def clearNeighbors {
    neighbors = new Array[Triangle](3)
  }
  
  def oppositePoint(t: Triangle): Point = {
    
    assert(t != this, "self-pointer error")
    if(points(0) == t.points(1)) 
      points(1)
    else if(points(0) == t.points(2))
      points(2)
    else if(contains(t.points(1), t.points(2)))
      points(0)
    else {
      t.printDebug
      printDebug
      println(area + " | " + t.area)
      throw new Exception("Point location error, please report")
    }
    
  }
  
  // Fast point in triangle test
  def pointIn(point: Point): Boolean = {

    val ij = points(1) - points(0)
    val jk = points(2) - points(1)
    val pab = (point - points(0)).cross(ij)
    val pbc = (point - points(1)).cross(jk)
	var sameSign = Math.signum(pab) == Math.signum(pbc)
    if (!sameSign) return false

    val ki = points(0) - points(2)
	val pca = (point - points(2)).cross(ki)
	sameSign = Math.signum(pab) == Math.signum(pca)
    if (!sameSign) return false
    
    true
  }

  // Locate first triangle crossed by constrained edge
  def locateFirst(edge: Segment): Triangle = {
    
    if(contains(edge)) return this
    
    if(edge.q == points(0)) 
      search(points(1), points(2), edge, neighbors(2))
    else if(edge.q == points(1)) 
      search(points(0), points(2), edge, neighbors(0))
    else if(edge.q == points(2)) 
      search(points(0), points(1), edge, neighbors(1))
    else 
      throw new Exception("Point not found")

  }
  
  def search(p1: Point, p2: Point, edge: Segment, neighbor: Triangle): Triangle = {
    
      val o1 = Util.orient2d(edge.q, p1, edge.p)
      val o2 = Util.orient2d(edge.q, p2, edge.p)
      val sameSign = Math.signum(o1) == Math.signum(o2)
      
      // Edge crosses this triangle
      if(!sameSign) 
        return this
      
      // Look at neighbor
      if(neighbor == null) 
        null
      else
        neighbor.locateFirst(edge)
      
  }
  
  // Locate next triangle crossed by edge
  def findNeighbor(p: Point): Triangle = {
    
    if(contains(p)) return this
    
    if(Util.orient2d(points(1), points(0), p) > 0) 
      return neighbors(2)
    else if(Util.orient2d(points(2), points(1), p) > 0) 
      return neighbors(0)
    else if(Util.orient2d(points(0), points(2), p) > 0) 
      return neighbors(1)
    else
      return null
    
  }
  
  // The neighbor clockwise to given point
  def neighborCW(point: Point): Triangle = {
    
    if(point == points(0)) {
      neighbors(1)
    }else if(point == points(1)) {
      neighbors(2)
    } else 
      neighbors(0)
  }
  
  // The neighbor counter-clockwise to given point
  def neighborCCW(oPoint: Point): Triangle = {
    
    if(oPoint == points(0)) {
      neighbors(2)
    }else if(oPoint == points(1)) {
      neighbors(0)
    } else 
      neighbors(1)
  }
  
  // The neighbor clockwise to given point
  def neighborAcross(point: Point): Triangle = {
    
    if(point == points(0)) {
      neighbors(0)
    }else if(point == points(1)) {
      neighbors(1)
    } else 
      neighbors(2)
  }
  
  // The point counter-clockwise to given point
  def pointCCW(point: Point): Point = {
    
    if(point == points(0)) {
      points(1)
    } else if(point == points(1)) {
      points(2)
    } else if(point == points(2)){
      points(0)
    } else {
      throw new Exception("point location error")
    }
  }
  
  // The point counter-clockwise to given point
  def pointCW(point: Point): Point = {
    
    if(point == points(0)) {
      points(2)
    } else if(point == points(1)) {
      points(0)
    } else if(point == points(2)){
      points(1)
    } else {
      throw new Exception("point location error")
    }
  }
  
  // Legalized triangle by rotating clockwise around point(0)
  def legalize(oPoint: Point) {
    
	points(1) = points(0)
	points(0) = points(2)
	points(2) = oPoint
  }
  
  // Legalize triagnle by rotating clockwise around oPoint
  def legalize(oPoint: Point, nPoint: Point) {
    
    if(oPoint == points(0)) {
      points(1) = points(0)
      points(0) = points(2)
      points(2) = nPoint 
    } else if (oPoint == points(1)) {
      points(2) = points(1)
      points(1) = points(0)
      points(0) = nPoint
    } else if (oPoint == points(2)) {
      points(0) = points(2)
      points(2) = points(1)
      points(1) = nPoint
    } else {
      throw new Exception("legalization error")
    }
    
  }
  
  // Check if legalized triangle will be collinear
  def collinear(oPoint: Point): Boolean = Util.collinear(points(1), points(0), oPoint)
  
  // Check if legalized triangle will be collinear
  def collinear(oPoint: Point, nPoint: Point): Boolean = {
    
    if(oPoint == points(0)) {
      Util.collinear(points(0), points(2), nPoint)
    } else if (oPoint == points(1)) {
      Util.collinear(points(0), points(1), nPoint)
    } else {
      Util.collinear(points(2), points(1), nPoint)
    }
  }
  
  // Rotate neighbors clockwise around give point. Share diagnal with triangle
  def rotateNeighborsCW(oPoint: Point, triangle: Triangle) {
    
    if(oPoint == points(0)) {
      neighbors(2) = neighbors(1)
      neighbors(1) = null
      neighbors(0) = triangle
    } else if (oPoint == points(1)) {
      neighbors(0) = neighbors(2)
      neighbors(2) = null
      neighbors(1) = triangle
    } else if (oPoint == points(2)) {
      neighbors(1) = neighbors(0)
      neighbors(0) = null
      neighbors(2) = triangle
    } else {
      throw new Exception("pointer bug")
    }
  }
  
  def printDebug = println(points(0) + "," + points(1) + "," + points(2))

  // Finalize edge marking
  def markNeighborEdges {
    
    for(i <- 0 to 2) 
      if(edges(i)) 
        i match {
          case 0 => if(neighbors(0) != null) neighbors(0).markEdge(points(1), points(2))
          case 1 => if(neighbors(1) != null) neighbors(1).markEdge(points(0), points(2))
          case 2 => if(neighbors(2) != null) neighbors(2).markEdge(points(0), points(1))
        }
  }
  
  def markEdge(triangle: Triangle) {
    
    for(i <- 0 to 2) 
      if(edges(i)) 
        i match {
          case 0 => triangle.markEdge(points(1), points(2))
          case 1 => triangle.markEdge(points(0), points(2))
          case 2 => triangle.markEdge(points(0), points(1))
        }
  }
  
  def markEdge(T: ArrayBuffer[Triangle]) {
    
    for(t <- T) {
	    for(i <- 0 to 2) 
	      if(t.edges(i)) 
	        i match {
	          case 0 => markEdge(t.points(1), t.points(2))
	          case 1 => markEdge(t.points(0), t.points(2))
	          case 2 => markEdge(t.points(0), t.points(1))
	        }
     }
  }
  
  // Mark edge as constrained
  def markEdge(p: Point, q: Point) {
    
    if((q == points(0) && p == points(1)) || (q == points(1) && p == points(0))) {
      finalized = true
      edges(2) = true
    } else if ((q == points(0) && p == points(2)) || (q == points(2) && p == points(0))) {
      edges(1) = true
      finalized = true
    } else if ((q == points(1) && p == points(2)) || (q == points(2) && p == points(1))){
      finalized = true
      edges(0) = true
    } 
  }
  
  def area = {
    
    val b = points(0).x - points(1).x
    val h = points(2).y - points(1).y
    
    Math.abs((b*h*0.5f))
  }
  
  def centroid: Point = {
    
    val cx = (points(0).x + points(1).x + points(2).x)/3f
    val cy = (points(0).y + points(1).y + points(2).y)/3f
    Point(cx, cy)
  }
  
  def thin: Boolean = {
     val a1 = (points(1) - points(0))
	 val b1 = (points(2) - points(0))
     val a2 = (points(0) - points(1))
     val b2 = (points(2) - points(1))
	 val angle1 = Math.abs(Math.atan2(a1 cross b1, a1 dot b1))
     val angle2 = Math.abs(Math.atan2(a2 cross b2, a2 dot b2))
	 val angle3 = Math.Pi - angle1 - angle2
     // 30 degrees
     val minAngle = Math.Pi/6
     (angle1 <= minAngle || angle2 <= minAngle || angle3 <= minAngle)
  } 
  
  // Compute barycentric coordinates (u, v, w) for
  // point p with respect to triangle 
  // From "Real-Time Collision Detection" by Christer Ericson
  def barycentric(p: Point): List[Float] = {
    val v0 = points(1) - points(0)
    val v1 = points(2) - points(0)
    val v2 = p - points(0)
    val d00 = v0 dot v0
    val d01 = v0 dot v1
    val d11 = v1 dot v1
    val d20 = v2 dot v0
    val d21 = v2 dot v1
    val denom = d00 * d11 - d01 * d01
    val v = (d11 * d20 - d01 * d21) / denom
    val w = (d00 * d21 - d01 * d20) / denom
    val u = 1.0f - v - w
    List(u, v, w)
 }
  
}
