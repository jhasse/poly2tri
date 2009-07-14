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

import collection.jcl.ArrayList
import scala.collection.mutable.{Map, HashSet}

// See "Computational Geometry", 3rd edition, by Mark de Berg et al, Chapter 6.2

class TrapezoidalMap {

  // Trapezoid associated array
  val map = HashSet.empty[Trapezoid]
  // AABB margin
  var margin = 20f
    
  // Bottom segment that spans multiple trapezoids
  private var bCross: Segment = null
  // Top segment that spans multiple trapezoids
  private var tCross: Segment = null
  
  // Add a trapezoid to the map
  def add(t: Trapezoid) {
    map += t
  }
  
  // Remove a trapezoid from the map
  def remove(t: Trapezoid) {
    map -=t
  }
  
  def reset {
    bCross = null
    tCross = null
  }

  // Case 1: segment completely enclosed by trapezoid
  //         break trapezoid into 4 smaller trapezoids
  def case1(t: Trapezoid, s: Segment) = {
    
    assert(s.p.x != s.q.x)
    
    val trapezoids = new ArrayList[Trapezoid]
    trapezoids += new Trapezoid(t.leftPoint, s.p, t.top, t.bottom)
    trapezoids += new Trapezoid(s.p, s.q, t.top, s)
    trapezoids += new Trapezoid(s.p, s.q, s, t.bottom)
    trapezoids += new Trapezoid(s.q, t.rightPoint, t.top, t.bottom)
    
    trapezoids(0).update(t.upperLeft, t.lowerLeft, trapezoids(1), trapezoids(2))
    trapezoids(1).update(trapezoids(0), null, trapezoids(3), null)
    trapezoids(2).update(null, trapezoids(0), null, trapezoids(3))
    trapezoids(3).update(trapezoids(1), trapezoids(2), t.upperRight, t.lowerRight)
    
    s.above = trapezoids(1)
    s.below = trapezoids(2)
    
    t.updateNeighbors(trapezoids(0), trapezoids(0), trapezoids(3), trapezoids(3))
    trapezoids
  }

  // Case 2: Trapezoid contains point p, q lies outside
  //         break trapezoid into 3 smaller trapezoids
  def case2(t: Trapezoid, s: Segment) = {
    
    val rp = if(s.q.x == t.rightPoint.x) s.q else t.rightPoint
    
    val trapezoids = new ArrayList[Trapezoid]
    trapezoids += new Trapezoid(t.leftPoint, s.p, t.top, t.bottom)
    trapezoids += new Trapezoid(s.p, rp, t.top, s)
    trapezoids += new Trapezoid(s.p, rp, s, t.bottom)
   
    trapezoids(0).update(t.upperLeft, t.lowerLeft, trapezoids(1), trapezoids(2))
    trapezoids(1).update(trapezoids(0), null, t.upperRight, null)
    trapezoids(2).update(null, trapezoids(0), null, t.lowerRight)
    
    bCross = t.bottom
    tCross = t.top
    s.above = trapezoids(1)
    s.below = trapezoids(2)
    
    t.updateNeighbors(trapezoids(0), trapezoids(0), trapezoids(1), trapezoids(2))
    trapezoids
  }
  
  // Case 3: Trapezoid is bisected
  def case3(t: Trapezoid, s: Segment) = {
    
    assert(s.p.x != s.q.x)
    
    val lp = if(s.p.x == t.leftPoint.x) s.p else t.leftPoint
    val rp = if(s.q.x == t.rightPoint.x) s.q else t.rightPoint
    
    val topCross = (tCross == t.top)
    val bottomCross = (bCross == t.bottom)
    
    val trapezoids = new ArrayList[Trapezoid]
    trapezoids += {if(topCross) t.upperLeft else new Trapezoid(lp, rp, t.top, s)}
    trapezoids += {if(bottomCross) t.lowerLeft else new Trapezoid(lp, rp, s, t.bottom)}
    
    if(topCross) {
      trapezoids(0).upperRight = t.upperRight
      trapezoids(0).rightPoint = t.rightPoint
    } else {
      trapezoids(0).update(t.upperLeft, s.above, t.upperRight, null)
      if(s.above != null) s.above.lowerRight = trapezoids(0)
    }
    
    if(bottomCross) {
      trapezoids(1).lowerRight = t.lowerRight
      trapezoids(1).rightPoint = t.rightPoint
    } else {
      trapezoids(1).update(s.below, t.lowerLeft, null, t.lowerRight)
      if(s.below != null) s.below.upperRight = trapezoids(1)
    }
    
    bCross = t.bottom
    tCross = t.top
    s.above = trapezoids(0)
    s.below = trapezoids(1)
    
    t.updateNeighbors(trapezoids(0), trapezoids(1), trapezoids(0), trapezoids(1))
    trapezoids
  }
  
  // Case 4: Trapezoid contains point q, p lies outside
  //         break trapezoid into 3 smaller trapezoids
  def case4(t: Trapezoid, s: Segment) = {
    
    val lp = if(s.p.x == t.leftPoint.x) s.p else t.leftPoint
    
    val topCross = (tCross == t.top)
    val bottomCross = (bCross == t.bottom)
    
    val trapezoids = new ArrayList[Trapezoid]
    trapezoids += {if(topCross) t.upperLeft else new Trapezoid(lp, s.q, t.top, s)}
    trapezoids += {if(bottomCross) t.lowerLeft else new Trapezoid(lp, s.q, s, t.bottom)}
    trapezoids += new Trapezoid(s.q, t.rightPoint, t.top, t.bottom)
    
    if(topCross) {
      trapezoids(0).upperRight = trapezoids(2)
      trapezoids(0).rightPoint = s.q
    } else {
      trapezoids(0).update(t.upperLeft, s.above, trapezoids(2), null)
      if(s.above != null) s.above.lowerRight = trapezoids(0)
    }
    
    if(bottomCross) {
      trapezoids(1).lowerRight = trapezoids(2)
      trapezoids(1).rightPoint = s.q
    } else {
      trapezoids(1).update(s.below, t.lowerLeft, null, trapezoids(2))
      if(s.below != null) s.below.upperRight = trapezoids(1)
    }
    trapezoids(2).update(trapezoids(0), trapezoids(1), t.upperRight, t.lowerRight)
    
    s.above = trapezoids(0)
    s.below = trapezoids(1)
    
    t.updateNeighbors(trapezoids(0), trapezoids(1), trapezoids(2), trapezoids(2))
    trapezoids
  }
  
  // Create an AABB around segments
  def boundingBox(segments: ArrayList[Segment]): Trapezoid = {
   
    var max = segments(0).p + margin
    var min = segments(0).q + margin

    for(s <- segments) {
      if(s.p.x > max.x) max = new Point(s.p.x + margin, max.y)
      if(s.p.y > max.y) max = new Point(max.x, s.p.y + margin)
      if(s.q.x > max.x) max = new Point(s.q.x+margin, max.y)
      if(s.q.y > max.y) max = new Point(max.x, s.q.y+margin)
      if(s.p.x < min.x) min = new Point(s.p.x-margin, min.y)
      if(s.p.y < min.y) min = new Point(min.x, s.p.y-margin)
      if(s.q.x < min.x) min = new Point(s.q.x-margin, min.y)
      if(s.q.y < min.y) min = new Point(min.x, s.q.y-margin)
    }

    val top = new Segment(new Point(min.x, max.y), new Point(max.x, max.y))
    val bottom = new Segment(new Point(min.x, min.y), new Point(max.x, min.y))
    val left = bottom.p
    val right = top.q
    
    return new Trapezoid(left, right, top, bottom)
  }
}
