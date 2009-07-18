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
package org.poly2tri

import scala.collection.mutable.{HashSet, ArrayBuffer}

// See "Computational Geometry", 3rd edition, by Mark de Berg et al, Chapter 6.2

class TrapezoidalMap {

  // Trapezoid associated array
  val map = HashSet.empty[Trapezoid]
  // AABB margin
  var margin = 50f
    
  // Bottom segment that spans multiple trapezoids
  private var bCross: Segment = null
  // Top segment that spans multiple trapezoids
  private var tCross: Segment = null
  
  // Add a trapezoid to the map
  def add(t: Trapezoid) {
    assert(t != null, "Bad value")
    map += t
  }
  
  // Remove a trapezoid from the map
  def remove(t: Trapezoid) {
    assert(t != null, "Bad value")
    map -=t
  }
  
  def reset {
    bCross = null
    tCross = null
  }

  // Case 1: segment completely enclosed by trapezoid
  //         break trapezoid into 4 smaller trapezoids
  def case1(t: Trapezoid, s: Segment) = {
    
    val trapezoids = new Array[Trapezoid](4)
    trapezoids(0) = new Trapezoid(t.leftPoint.clone, s.p.clone, t.top, t.bottom)
    trapezoids(1) = new Trapezoid(s.p.clone, s.q.clone, t.top, s)
    trapezoids(2) = new Trapezoid(s.p.clone, s.q.clone, s, t.bottom)
    trapezoids(3) = new Trapezoid(s.q.clone, t.rightPoint.clone, t.top, t.bottom)
    
    trapezoids(0).update(t.upperLeft, t.lowerLeft, trapezoids(1), trapezoids(2))
    trapezoids(1).update(trapezoids(0), null, trapezoids(3), null)
    trapezoids(2).update(null, trapezoids(0), null, trapezoids(3))
    trapezoids(3).update(trapezoids(1), trapezoids(2), t.upperRight, t.lowerRight)
    
    s.above = trapezoids(1)
    s.below = trapezoids(2)
    
    trapezoids
  }

  // Case 2: Trapezoid contains point p, q lies outside
  //         break trapezoid into 3 smaller trapezoids
  def case2(t: Trapezoid, s: Segment) = {
    
    assert(s.p.x < s.q.x)
    
    val trapezoids = new Array[Trapezoid](3)
    trapezoids(0) = new Trapezoid(t.leftPoint.clone, s.p.clone, t.top, t.bottom)
    trapezoids(1) = new Trapezoid(s.p.clone, t.rightPoint.clone, t.top, s)
    trapezoids(2) = new Trapezoid(s.p.clone, t.rightPoint.clone, s, t.bottom)
   
    trapezoids(0).update(t.upperLeft, t.lowerLeft, trapezoids(1), trapezoids(2))
    trapezoids(1).update(trapezoids(0), null, t.upperRight, null)
    trapezoids(2).update(null, trapezoids(0), null, t.lowerRight)
    
    bCross = t.bottom
    tCross = t.top
    s.above = trapezoids(1)
    s.below = trapezoids(2)
    
    trapezoids
  }
  
  // Case 3: Trapezoid is bisected
  def case3(t: Trapezoid, s: Segment) = {
    
    val topCross = (tCross == t.top)
    val bottomCross = (bCross == t.bottom)
    
    val trapezoids = new Array[Trapezoid](2)
    trapezoids(0) = if(topCross) t.upperLeft else new Trapezoid(t.leftPoint.clone, t.rightPoint.clone, t.top, s)
    trapezoids(1) = if(bottomCross) t.lowerLeft else new Trapezoid(t.leftPoint.clone, t.rightPoint.clone, s, t.bottom)
    
    if(topCross) {
      trapezoids(0).upperRight = t.upperRight
      if(t.upperRight != null) t.upperRight.upperLeft = trapezoids(0)
      trapezoids(0).rightPoint = t.rightPoint.clone
    } else {
      trapezoids(0).update(t.upperLeft, s.above, t.upperRight, null)
    }
    
    if(bottomCross) {
      trapezoids(1).lowerRight = t.lowerRight
      if(t.lowerRight != null) t.lowerRight.lowerLeft = trapezoids(1)
      trapezoids(1).rightPoint = t.rightPoint.clone
    } else {
      trapezoids(1).update(s.below, t.lowerLeft, null, t.lowerRight)
    }
    
    bCross = t.bottom
    tCross = t.top
    s.above = trapezoids(0)
    s.below = trapezoids(1)
    
    trapezoids
  }
  
  // Case 4: Trapezoid contains point q, p lies outside
  //         break trapezoid into 3 smaller trapezoids
  def case4(t: Trapezoid, s: Segment) = {
       
    val topCross = (tCross == t.top)
    val bottomCross = (bCross == t.bottom)

    val trapezoids = new Array[Trapezoid](3)
    trapezoids(0) = if(topCross) t.upperLeft else new Trapezoid(t.leftPoint.clone, s.q.clone, t.top, s)
    trapezoids(1) = if(bottomCross) t.lowerLeft else new Trapezoid(t.leftPoint.clone, s.q.clone, s, t.bottom)
    trapezoids(2) = new Trapezoid(s.q.clone, t.rightPoint.clone, t.top, t.bottom)
    
    if(topCross) {
      trapezoids(0).upperRight = trapezoids(2)
      trapezoids(0).rightPoint = s.q.clone
    } else {
      trapezoids(0).update(t.upperLeft, s.above, trapezoids(2), null)
    }
    
    if(bottomCross) {
      trapezoids(1).lowerRight = trapezoids(2)
      trapezoids(1).rightPoint = s.q.clone
    } else {
      trapezoids(1).update(s.below, t.lowerLeft, null, trapezoids(2))
    }
    
    trapezoids(2).update(trapezoids(0), trapezoids(1), t.upperRight, t.lowerRight)
    
    s.above = trapezoids(0)
    s.below = trapezoids(1)
    
    trapezoids
  }
  
  // Create an AABB around segments
  def boundingBox(segments: ArrayBuffer[Segment]): Trapezoid = {
   
    var max = segments(0).p.clone + margin
    var min = segments(0).q.clone - margin

    for(s <- segments) {
      if(s.p.clone.x > max.x) max = Point(s.p.x + margin, max.y)
      if(s.p.clone.y > max.y) max = Point(max.x, s.p.y + margin)
      if(s.q.clone.x > max.x) max = Point(s.q.x+margin, max.y)
      if(s.q.clone.y > max.y) max = Point(max.x, s.q.y+margin)
      if(s.p.clone.x < min.x) min = Point(s.p.x-margin, min.y)
      if(s.p.clone.y < min.y) min = Point(min.x, s.p.y-margin)
      if(s.q.clone.x < min.x) min = Point(s.q.x-margin, min.y)
      if(s.q.clone.y < min.y) min = Point(min.x, s.q.y-margin)
    }

    val top = new Segment(Point(min.x, max.y), Point(max.x, max.y))
    val bottom = new Segment(Point(min.x, min.y), Point(max.x, min.y))
    val left = bottom.p.clone
    val right = bottom.q.clone
    
    return new Trapezoid(left, right, top, bottom)
  }
}
