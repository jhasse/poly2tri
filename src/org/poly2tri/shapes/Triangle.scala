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

// Triangle-based data structures are know to have better performance than quad-edge structures
// See: J. Shewchuk, "Triangle: Engineering a 2D Quality Mesh Generator and Delaunay Triangulator"
//      "Triangulations in CGAL"
class Triangle(val points: Array[Point], val neighbors: Array[Triangle]) {

  var ik, ij , jk, ji, kj, ki: Point = null
  updatePoints
  
  // Flags to determine if an edge is the final Delauney edge
  val edges = new Array[Boolean](3)
  
  def updatePoints {
    ik = points(2) - points(0)
    ij = points(1) - points(0)
    jk = points(2) - points(1)
    ji = points(0) - points(1)
    kj = points(1) - points(2)
    ki = points(0) - points(2)
  }
  
  // Update neighbor pointers
  def updateNeighbors(ccwPoint: Point, cwPoint: Point, triangle: Triangle) {
    if(ccwPoint == points(2) && cwPoint == points(1)) 
      neighbors(0) = triangle 
    else if(ccwPoint == points(0) && cwPoint == points(2))
      neighbors(1) = triangle
    else 
      neighbors(2) = triangle
  }
  
  def oppositePoint(t: Triangle) = {
    if(points(0) == t.points(1)) 
      points(1)
    else if(points(0) == t.points(2))
      points(2)
    else
      points(0)
  }
  
  def contains(p: Point): Boolean = (p == points(0) || p == points(1) || p == points(2))
  def contains(e: Segment): Boolean = (contains(e.p) && contains(e.q))
  
  // Fast point in triangle test
  def pointIn(point: Point): Boolean = {

    val pab = (point - points(0)).cross(ij)
    val pbc = (point - points(1)).cross(jk)
	var sameSign = Math.signum(pab) == Math.signum(pbc)
    if (!sameSign) return false

	val pca = (point - points(2)).cross(ki)
	sameSign = Math.signum(pab) == Math.signum(pca)
    if (!sameSign) return false
    
    true
  }

  def locateFirst(edge: Segment): Triangle = {
    val p = edge.p
    if(contains(p)) return this
    val q = edge.q
    val e = p - q
    if(q == points(0)) {
      val sameSign = Math.signum(ik cross e) == Math.signum(ij cross e)
      if(!sameSign) return this
      if(neighbors(2) == null) return null
      return neighbors(2).locateFirst(edge)
    } else if(q == points(1)) {
      val sameSign = Math.signum(jk cross e) == Math.signum(ji cross e)
      if(!sameSign) return this
      if(neighbors(0) == null) return null
      return neighbors(0).locateFirst(edge)
    } else if(q == points(2)) {
      val sameSign = Math.signum(kj cross e) == Math.signum(ki cross e)
      if(!sameSign) return this
      if(neighbors(1) == null) return null
      return neighbors(1).locateFirst(edge)
    }
    null
  }
  
  def findNeighbor(e: Point): Triangle = {
    var sameSign = Math.signum(ik cross e) == Math.signum(ij cross e)
    if(!sameSign) return neighbors(0)
    sameSign = Math.signum(jk cross e) == Math.signum(ji cross e)
    if(!sameSign) return neighbors(1)
    sameSign = Math.signum(kj cross e) == Math.signum(ki cross e)
    if(!sameSign) return neighbors(2)
    this
  }
  
  def printDebug {
    println("**************")
    println(points(0) + "," + points(1) + "," + points(2))
  }
  
}
