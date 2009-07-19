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
class Triangulator(segments: ArrayBuffer[Segment]) {
  
  // Triangle decomposition list
  var triangles = new ArrayBuffer[Array[Point]]
  
  // Order and randomize the segments
  val segmentList = orderSegments
  
  // Build the trapezoidal map and query graph
  def process {
    
    for(s <- segmentList) {
      var traps = queryGraph.followSegment(s)
      // Remove trapezoids from trapezoidal Map
      traps.foreach(trapezoidalMap.remove)
      for(t <- traps) {
        var tList: Array[Trapezoid] = null
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
        // Add new trapezoids to map
        tList.foreach(trapezoidalMap.add)
      }
      trapezoidalMap reset
    }

    // Mark outside trapezoids
    trapezoidalMap.map.foreach(markOutside)
    
    // Collect interior trapezoids
    for(t <- trapezoidalMap.map) 
      if(t.inside) {
        trapezoids += t
        t addPoints
      }
   
    createMountains
    
    // Extract all the triangles into a single list
    for(i <- 0 until xMonoPoly.size)
      for(t <- xMonoPoly(i).triangles)
    	  triangles += t
    
    //println("# triangles = " + triangles.size)
  }
  
  // The trapezoidal map 
  def trapezoidMap = trapezoidalMap.map
  // Trapezoid decomposition list
  var trapezoids = new ArrayBuffer[Trapezoid]
  
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
  private val queryGraph = new QueryGraph(Sink.init(boundingBox))
  private val xMonoPoly = new ArrayBuffer[MonotoneMountain]
                                        
  // Build a list of x-monotone mountains
  private def createMountains {
    for(s <- segmentList) {
      if(s.mPoints.size > 0) {
         val mountain = new MonotoneMountain
         val k = Util.msort((p1: Point, p2: Point) => p1 < p2)(s.mPoints.toList)
         val points = s.p :: k ::: List(s.q)
         points.foreach(p => mountain += p)
         mountain.triangulate
         xMonoPoly += mountain
      }
    }   
  }
  
  // Mark the outside trapezoids surrounding the polygon
  private def markOutside(t: Trapezoid) {
	  if(t.top == boundingBox.top || t.bottom == boundingBox.bottom) {
	    t trimNeighbors
	  }
  }
  
  private def orderSegments = {
    // Ignore vertical segments!
    val segs = new ArrayBuffer[Segment]
    for(s <- segments) 
      // Point p must be to the left of point q
      if(s.p.x > s.q.x) {
        segs += new Segment(s.q.clone, s.p.clone)
      } else if(s.p.x < s.q.x)
        segs += new Segment(s.p.clone, s.q.clone)
    // Randomized triangulation improves performance
    // See Seidel's paper, or O'Rourke's book, p. 57 
    // Turn this off for while bug hunting math robustness issues
    //Random.shuffle(segs)
    segs
  }
}
