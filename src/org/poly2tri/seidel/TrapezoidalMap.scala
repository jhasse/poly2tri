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
package org.poly2tri.seidel

import scala.collection.mutable.{HashSet, ArrayBuffer}

import shapes.{Point, Segment, Trapezoid}

// See "Computational Geometry", 3rd edition, by Mark de Berg et al, Chapter 6.2

class TrapezoidalMap {

  // Trapezoid container
  val map = HashSet.empty[Trapezoid]
  // AABB margin
  var margin = 50f
    
  // Bottom segment that spans multiple trapezoids
  private var bCross: Segment = null
  // Top segment that spans multiple trapezoids
  private var tCross: Segment = null
  
  def clear {
    bCross = null
    tCross = null
  }

  // Case 1: segment completely enclosed by trapezoid
  //         break trapezoid into 4 smaller trapezoids
  def case1(t: Trapezoid, s: Segment) = {
    
    val trapezoids = new Array[Trapezoid](4)
    trapezoids(0) = new Trapezoid(t.leftPoint, s.p, t.top, t.bottom)
    trapezoids(1) = new Trapezoid(s.p, s.q, t.top, s)
    trapezoids(2) = new Trapezoid(s.p, s.q, s, t.bottom)
    trapezoids(3) = new Trapezoid(s.q, t.rightPoint, t.top, t.bottom)
    
    trapezoids(0).updateLeft(t.upperLeft, t.lowerLeft)
    trapezoids(1).updateLeftRight(trapezoids(0), null, trapezoids(3), null)
    trapezoids(2).updateLeftRight(null, trapezoids(0), null, trapezoids(3))
    trapezoids(3).updateRight(t.upperRight, t.lowerRight)
    
    trapezoids
  }

  // Case 2: Trapezoid contains point p, q lies outside
  //         break trapezoid into 3 smaller trapezoids
  def case2(t: Trapezoid, s: Segment) = {
    
    val rp = if(s.q.x == t.rightPoint.x) s.q else t.rightPoint
    
    val trapezoids = new Array[Trapezoid](3)
    trapezoids(0) = new Trapezoid(t.leftPoint, s.p, t.top, t.bottom)
    trapezoids(1) = new Trapezoid(s.p, rp, t.top, s)
    trapezoids(2) = new Trapezoid(s.p, rp, s, t.bottom)
   
    trapezoids(0).updateLeft(t.upperLeft, t.lowerLeft)
    trapezoids(1).updateLeftRight(trapezoids(0), null, t.upperRight, null)
    trapezoids(2).updateLeftRight(null, trapezoids(0), null, t.lowerRight)
    
    bCross = t.bottom
    tCross = t.top
    
    s.above = trapezoids(1)
    s.below = trapezoids(2)
    
    trapezoids
  }
  
  // Case 3: Trapezoid is bisected
  def case3(t: Trapezoid, s: Segment) = {
    
    val lp = if(s.p.x == t.leftPoint.x) s.p else t.leftPoint
    val rp = if(s.q.x == t.rightPoint.x) s.q else t.rightPoint
    
    val trapezoids = new Array[Trapezoid](2)

    if(tCross == t.top) {
      trapezoids(0) = t.upperLeft
      trapezoids(0).updateRight(t.upperRight, null)
      trapezoids(0).rightPoint = rp
    } else {
      trapezoids(0) = new Trapezoid(lp, rp, t.top, s)
      trapezoids(0).updateLeftRight(t.upperLeft, s.above, t.upperRight, null)
    }
    
    if(bCross == t.bottom) {
      trapezoids(1) = t.lowerLeft
      trapezoids(1).updateRight(null, t.lowerRight)
      trapezoids(1).rightPoint = rp
    } else {
      trapezoids(1) =  new Trapezoid(lp, rp, s, t.bottom)
      trapezoids(1).updateLeftRight(s.below, t.lowerLeft, null, t.lowerRight)
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
      
    val lp = if(s.p.x == t.leftPoint.x) s.p else t.leftPoint

    val trapezoids = new Array[Trapezoid](3)
    
    if(tCross == t.top) {
      trapezoids(0) = t.upperLeft
      trapezoids(0).rightPoint = s.q
    } else {
      trapezoids(0) = new Trapezoid(lp, s.q, t.top, s)
      trapezoids(0).updateLeft(t.upperLeft, s.above)
    }
    
    if(bCross == t.bottom) {
      trapezoids(1) = t.lowerLeft
      trapezoids(1).rightPoint = s.q
    } else {
      trapezoids(1) = new Trapezoid(lp, s.q, s, t.bottom)
      trapezoids(1).updateLeft(s.below, t.lowerLeft)
    }
    
    trapezoids(2) = new Trapezoid(s.q, t.rightPoint, t.top, t.bottom)
    trapezoids(2).updateLeftRight(trapezoids(0), trapezoids(1), t.upperRight, t.lowerRight)
    
    trapezoids
  }
  
  // Create an AABB around segments
  def boundingBox(segments: ArrayBuffer[Segment]): Trapezoid = {
   
    var max = segments(0).p + margin
    var min = segments(0).q - margin

    for(s <- segments) {
      if(s.p.x > max.x) max = Point(s.p.x + margin, max.y)
      if(s.p.y > max.y) max = Point(max.x, s.p.y + margin)
      if(s.q.x > max.x) max = Point(s.q.x+margin, max.y)
      if(s.q.y > max.y) max = Point(max.x, s.q.y+margin)
      if(s.p.x < min.x) min = Point(s.p.x-margin, min.y)
      if(s.p.y < min.y) min = Point(min.x, s.p.y-margin)
      if(s.q.x < min.x) min = Point(s.q.x-margin, min.y)
      if(s.q.y < min.y) min = Point(min.x, s.q.y-margin)
    }

    val top = new Segment(Point(min.x, max.y), Point(max.x, max.y))
    val bottom = new Segment(Point(min.x, min.y), Point(max.x, min.y))
    val left = bottom.p
    val right = top.q
    
    return new Trapezoid(left, right, top, bottom)
  }
}
