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

import seidel.Sink

class Trapezoid(val leftPoint: Point, var rightPoint: Point, val top: Segment, val bottom: Segment) {

  var sink: Sink = null
  var inside = true
  
  // Neighbor pointers
  var upperLeft: Trapezoid = null
  var lowerLeft: Trapezoid = null
  var upperRight: Trapezoid = null
  var lowerRight: Trapezoid = null
  
  // Update neighbors to the left
  def updateLeft(ul: Trapezoid, ll: Trapezoid) {
    upperLeft = ul; if(ul != null) ul.upperRight = this
    lowerLeft = ll; if(ll != null) ll.lowerRight = this  
  }
  
  // Update neighbors to the right
  def updateRight(ur: Trapezoid, lr: Trapezoid) {
    upperRight = ur; if(ur != null) ur.upperLeft = this
    lowerRight = lr; if(lr != null) lr.lowerLeft = this    
  }
  
  // Update neighbors on both sides
  def updateLeftRight(ul: Trapezoid, ll: Trapezoid, ur: Trapezoid, lr: Trapezoid) {
    upperLeft = ul; if(ul != null) ul.upperRight = this
    lowerLeft = ll; if(ll != null) ll.lowerRight = this
    upperRight = ur; if(ur != null) ur.upperLeft = this
    lowerRight = lr; if(lr != null) lr.lowerLeft = this    
  }
  
  // Recursively trim outside neighbors
  def trimNeighbors {
    if(inside) {
      inside = false
      if(upperLeft != null) upperLeft.trimNeighbors
      if(lowerLeft != null) lowerLeft.trimNeighbors
      if(upperRight != null) upperRight.trimNeighbors
      if(lowerRight != null) lowerRight.trimNeighbors
    }
  }
  
  // Determines if this point lies inside the trapezoid
  def contains(point: Point) = {
    (point.x > leftPoint.x && point.x < rightPoint.x && top > point && bottom < point)
  }
  
  def vertices: Array[Point] = {
    val verts = new Array[Point](4)
    verts(0) = lineIntersect(top, leftPoint.x)
    verts(1) = lineIntersect(bottom, leftPoint.x)
    verts(2) = lineIntersect(bottom, rightPoint.x)
    verts(3) = lineIntersect(top, rightPoint.x)
    verts
  }
  
  def lineIntersect(s: Segment, x: Float) = {
    val y =  s.slope * x + s.b
    Point(x, y)
  } 
  
  // Add points to monotone mountain
  def addPoints {
    if(leftPoint != bottom.p) {bottom.mPoints += leftPoint.clone}
    if(rightPoint != bottom.q) {bottom.mPoints += rightPoint.clone}
    if(leftPoint != top.p) {top.mPoints += leftPoint.clone}
    if(rightPoint != top.q) {top.mPoints += rightPoint.clone}
  }
  
  def debugData {
    println("LeftPoint = " + leftPoint + " | RightPoint = " + rightPoint)
    println("Top Segment: p = " + top.p + ", q = " + top.q)
    println("Bottom Segment: p = " + bottom.p + ", q = " + bottom.q)
  }
}
