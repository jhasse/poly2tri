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

import scala.collection.mutable.ArrayBuffer

import utils.{Util, Random}
import shapes.{Point, Segment, Trapezoid}

// Based on Raimund Seidel's paper "A simple and fast incremental randomized
// algorithm for computing trapezoidal decompositions and for triangulating polygons"
// See also: "Computational Geometry", 3rd edition, by Mark de Berg et al, Chapter 6.2
//           "Computational Geometry in C", 2nd edition, by Joseph O'Rourke
class Triangulator(points: ArrayBuffer[Point]) {
  
  // Convex polygon list
  var polygons = new ArrayBuffer[Array[Point]]
  // Order and randomize the segments
  val segmentList = initSegments
  
  // The trapezoidal map 
  def trapezoidMap = trapezoidalMap.map
  // Trapezoid decomposition list
  var trapezoids = new ArrayBuffer[Trapezoid]
  
  // Initialize trapezoidal map and query structure
  private val trapezoidalMap = new TrapezoidalMap
  private val boundingBox = trapezoidalMap.boundingBox(segmentList)
  private val queryGraph = new QueryGraph(Sink.init(boundingBox))
  private val xMonoPoly = new ArrayBuffer[MonotoneMountain]
  
  process
  
  // Build the trapezoidal map and query graph
  private def process {

    var i = 0
    while(i < segmentList.size) {
      
      val s = segmentList(i)
      var traps = queryGraph.followSegment(s)
      
      // Remove trapezoids from trapezoidal Map
      var j = 0
      while(j < traps.size) {
        trapezoidalMap.map -= traps(j)
        j += 1
      }
     
      j = 0
      while(j < traps.size) {
        val t = traps(j)
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
        var k = 0
        while(k < tList.size) {
          trapezoidalMap.map += tList(k)
          k += 1
        }
        j += 1
      }
      
      trapezoidalMap.clear
      i += 1
    
    }

    // Mark outside trapezoids
    for(t <- trapezoidalMap.map) 
      markOutside(t)
    
    // Collect interior trapezoids
    for(t <- trapezoidalMap.map) 
      if(t.inside) {
        trapezoids += t
        t addPoints
      }

    // Generate the triangles
    createMountains 
    
    //println("# triangles = " + triangles.size)
  }
  
  // Monotone polygons - these are monotone mountains
  def monoPolies: ArrayBuffer[ArrayBuffer[Point]] = {
    val polies = new ArrayBuffer[ArrayBuffer[Point]]
    for(i <- 0 until xMonoPoly.size)
     polies += xMonoPoly(i).monoPoly
    return polies
  }
  
  // Build a list of x-monotone mountains
  private def createMountains {
    
    var i = 0
    while(i < segmentList.size) {
      
      val s = segmentList(i)
      
      if(s.mPoints.size > 0) {
         
         val mountain = new MonotoneMountain
         var k: List[Point] = null
    
         // Sorting is a perfromance hit. Literature says this can be accomplised in
         // linear time, although I don't see a way around using traditional methods
         // when using a randomized incremental algorithm
         if(s.mPoints.size < 10) 
           // Insertion sort is one of the fastest algorithms for sorting arrays containing 
           // fewer than ten elements, or for lists that are already mostly sorted.
           k = Util.insertSort((p1: Point, p2: Point) => p1 < p2)(s.mPoints).toList
         else 
           k = Util.msort((p1: Point, p2: Point) => p1 < p2)(s.mPoints.toList)
         
         val points = s.p :: k ::: List(s.q)
         var j = 0
         while(j < points.size) {
           mountain += points(j)
           j += 1
         }
         
         // Triangulate monotone mountain
         mountain process
         
         // Extract the triangles into a single list
         j = 0
         while(j < mountain.triangles.size) {
    	   polygons += mountain.triangles(j)
           j += 1
         }
         
         xMonoPoly += mountain
      }
      i += 1
    }   
  }
  
  // Mark the outside trapezoids surrounding the polygon
  private def markOutside(t: Trapezoid) {
	  if(t.top == boundingBox.top || t.bottom == boundingBox.bottom) {
	    t trimNeighbors
	  }
  }
  
  // Create segments and connect end points; update edge event pointer
  private def initSegments: ArrayBuffer[Segment] = {
    var segments = List[Segment]()
    for(i <- 0 until points.size-1) 
      segments = new Segment(points(i), points(i+1)) :: segments
    segments =  new Segment(points.first, points.last) :: segments
    orderSegments(segments)
  }
  
  private def orderSegments(segments: List[Segment]) = {
    
    // Ignore vertical segments!
    val segs = new ArrayBuffer[Segment]
    for(s <- segments) {
      val p = shearTransform(s.p)
      val q = shearTransform(s.q)
      // Point p must be to the left of point q
      if(p.x > q.x) {
        segs += new Segment(q, p)
      } else if(p.x < q.x) {
        segs += new Segment(p, q)
      }
    }
    // Randomized triangulation improves performance
    // See Seidel's paper, or O'Rourke's book, p. 57 
    Random.shuffle(segs)
    segs
  }
  
  // Prevents any two distinct endpoints from lying on a common vertical line, and avoiding
  // the degenerate case. See Mark de Berg et al, Chapter 6.3
  //val SHEER = 0.0001f
  def shearTransform(point: Point) = Point(point.x + 0.0001f * point.y, point.y)
 
}
