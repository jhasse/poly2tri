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

import scala.collection.mutable.ArrayBuffer

// Based on Raimund Seidel's paper "A simple and fast incremental randomized
// algorithm for computing trapezoidal decompositions and for triangulating polygons"
class Triangulator(var segments: ArrayBuffer[Segment]) {
  
  // Trapezoid decomposition list
  var trapezoids : ArrayBuffer[Trapezoid] = null
  // Triangle decomposition list
  var triangles = new ArrayBuffer[Array[Point]]
  
  // Build the trapezoidal map and query graph
  def process {
    
    for(s <- segments) {
      val traps = queryGraph.followSegment(s)
      // Remove trapezoids from trapezoidal Map
      traps.foreach(trapezoidalMap.remove)
      for(t <- traps) {
        var tList: ArrayBuffer[Trapezoid] = null
        val containsP = t.contains(s.p)
        val containsQ = t.contains(s.q)
        if(containsP && containsQ) {
          // Case 1
          tList = trapezoidalMap.case1(t,s)
          queryGraph.case1(t.sink, s, tList)
        } else if(containsP && !containsQ) {
          // Case 2
          tList = trapezoidalMap.case2(t,s) 
          queryGraph.case2(t.sink, s, tList)
        } else if(!containsP && !containsQ) {
          // Case 3
          tList = trapezoidalMap.case3(t, s)
          queryGraph.case3(t.sink, s, tList)
        } else {
          // Case 4
          tList = trapezoidalMap.case4(t, s)
          queryGraph.case4(t.sink, s, tList)
        }
        // Add new trapezoids to the trapezoidal map
        tList.foreach(trapezoidalMap.add)
      }
      trapezoidalMap reset
    }
    trapezoids = trim
    createMountains
    
    // Extract all the triangles into a single list
    for(i <- 0 until xMonoPoly.size)
      for(t <- xMonoPoly(i).triangles)
    	  triangles += t
  }
  
  // For debugging
  def trapezoidMap = trapezoidalMap.map
  // Monotone polygons - these are monotone mountains
  def monoPolies: ArrayBuffer[ArrayBuffer[Point]] = {
    val polies = new ArrayBuffer[ArrayBuffer[Point]]
    for(i <- 0 until xMonoPoly.size)
     polies += xMonoPoly(i).monoPoly
    return polies
  }
  
  // Initialize trapezoidal map and query structure
  private val trapezoidalMap = new TrapezoidalMap
  private val boundingBox = trapezoidalMap.boundingBox(segments)
  private val queryGraph = new QueryGraph(new Sink(boundingBox))
  private val xMonoPoly = new ArrayBuffer[MonotoneMountain]
                                        
  segments = orderSegments
  
  // Build a list of x-monotone mountains
  private def createMountains {
    for(s <- segments) {
         val mountain = new MonotoneMountain
         val k = Util.msort((x: Point, y: Point) => x < y)(s.mPoints.toList)
         val points = s.p :: k ::: List(s.q)
         points.foreach(p => mountain += p.clone)
         if(mountain.size > 2) {
           mountain.triangulate
           xMonoPoly += mountain
         }
    }   
  }
  
  // Trim off the extraneous trapezoids surrounding the polygon
  private def trim = {
    val traps = new ArrayBuffer[Trapezoid]
    // Mark outside trapezoids
    for(t <- trapezoidalMap.map) {
	  if(t.top == boundingBox.top || t.bottom == boundingBox.bottom) {
	    t.outside = true
	    t.markNeighbors
	  }
    }
    // Collect interior trapezoids
    for(t <- trapezoidalMap.map) if(!t.outside) {
      traps += t
      t.mark
    }
    traps
  }
  
  private def orderSegments = {
    // Ignore vertical segments!
    val segs = new ArrayBuffer[Segment]
    for(s <- segments) {
      // Point p must be to the left of point q
      if(s.p.x > s.q.x) {
        val tmp = s.p
        s.p = s.q
        s.q = tmp
        segs += s
      } else if(s.p.x < s.q.x) {
        segs += s
      }
    }
    // This is actually important: See Seidel's paper
    Random.shuffle(segs)
  }
}
