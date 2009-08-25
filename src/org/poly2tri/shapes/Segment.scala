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

// Represents a simple polygon's edge
// TODO: Rename this class to Edge?
class Segment(var p: Point, var q: Point) {
 
  // Pointers used for building trapezoidal map
  var above, below: Trapezoid = null
  // Montone mountain points
  val mPoints = new ArrayBuffer[Point]
  // Equation of a line: y = m*x + b
  // Slope of the line (m)
  val slope = (q.y - p.y)/(q.x - p.x)
  // Y intercept
  val b = p.y - (p.x * slope)
  
  // Determines if this segment lies above the given point
  def > (point: Point) = (Math.floor(point.y) < Math.floor(slope * point.x + b))
  // Determines if this segment lies below the given point
  def < (point: Point) = (Math.floor(point.y) > Math.floor(slope * point.x + b))
  
  private def signedArea(a: Point, b: Point, c: Point): Float =
    (a.x - c.x) * (b.y - c.y) - (a.y - c.y) * (b.x - c.x)

  def intersect(c: Point, d: Point): Point = {
     
    val a = p
    val b = q
  
    val a1 = signedArea(a, b, d)
    val a2 = signedArea(a, b, c)

    if (a1 != 0.0f && a2 != 0.0f && a1*a2 < 0.0f) {
        val a3 = signedArea(c, d, a)
        val a4 = a3 + a2 - a1
        if (a3 * a4 < 0.0f) {
            val t = a3 / (a3 - a4)
            return a + ((b - a) * t)
        }
    }

    throw new Exception("Error")
    
  }

}
