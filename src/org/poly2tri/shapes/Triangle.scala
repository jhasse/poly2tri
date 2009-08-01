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

// Triangle-based data structures are know to have better performance than quad-edge structures
// See: J. Shewchuk, "Triangle: Engineering a 2D Quality Mesh Generator and Delaunay Triangulator"
//      "Triangulations in CGAL"
class Triangle(val points: Array[Point], val neighbors: Array[Triangle]) {

  // Flags to determine if an edge is the final Delauney edge
  val edges = new Array[Boolean](3)
  
  def contains(point: Point) = {
    if(point == points(0) || point == points(1) || point == points(2))
      true
    else 
      false
  }
  
  def legalize {
    
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
  
  // Fast point in triangle test
  def pointIn(point: Point): Boolean = {
    
    val ab = points(1) - points(0)
	val bc = points(2) - points(1)
	val ca = points(0) - points(2)

    val pab = (point - points(0)).cross(ab)
    val pbc = (point - points(1)).cross(bc)
	var sameSign = Math.signum(pab) == Math.signum(pbc)
    if (!sameSign) return false

	val pca = (point - points(2)).cross(ca)
	sameSign = Math.signum(pab) == Math.signum(pca)
    if (!sameSign) return false
    
    true
}

}
