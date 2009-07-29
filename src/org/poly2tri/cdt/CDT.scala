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
package org.poly2tri.cdt

import scala.collection.mutable.ArrayBuffer

import shapes.{Segment, Point, Triangle}
import utils.Util

/**
 * Sweep-line, Constrained Delauney Triangulation
 * See: Domiter, V. and Žalik, B.(2008)'Sweep-line algorithm for constrained Delaunay triangulation',
 *      International Journal of Geographical Information Science,22:4,449 — 462
 */
class CDT(segments: ArrayBuffer[Segment]) {

  // The initial triangle
  var initialTriangle: Triangle = null
  // The point list
  val points = init
  // The triangle mesh
  val mesh = new Mesh(initialTriangle)
  
  // Used to compute inital triangle
  private val ALPHA = 0.3f
  
  // Sweep points; build mesh
  sweep
  // Finalize triangulation
  finalization
  
  // Initialize and sort point list
  private def init: List[Point] = {
    
    var xmax, xmin = segments(0).p.x
    var ymax, ymin = segments(0).p.y
    val pts = new ArrayBuffer[Point]
    
    for(i <- 0 until segments.size) { 
      
      val p = segments(i).p
      val q = segments(i).q
      
      if(p.x > xmax) xmax = p.x
      if(q.x > xmax) xmax = q.x
      if(p.x < xmin) xmin = p.x
      if(q.x < xmin) xmin = q.x
      
      if(p.y > ymax) ymax = p.x
      if(q.y > ymax) ymax = q.x
      if(p.y < ymin) ymin = p.x
      if(q.y < ymin) ymin = q.x
      
      pts += shearTransform(p)
      pts += shearTransform(q)
    }
    
    var points: List[Point] = null
    
    if(pts.size < 10) 
     // Insertion sort is one of the fastest algorithms for sorting arrays containing 
     // fewer than ten elements, or for lists that are already mostly sorted.
     points = Util.insertSort((p1: Point, p2: Point) => p1 > p2)(pts).toList
    else
     // Merge sort: O(n log n)
     points = Util.msort((p1: Point, p2: Point) => p1 > p2)(pts.toList)
     
    val deltaX = ALPHA * (xmax - xmin)
    val deltaY = ALPHA * (ymax - ymin)
    
    val p1 = Point(xmin - deltaX, ymin - deltaY)
    val p2 = Point(xmax - deltaX, ymin - deltaY)
    
    initialTriangle = new Triangle(Array(p2, points(0), p1), null)
    points
  }
 
  // Implement sweep-line paradigm
  private def sweep {
  }  
  
  private def finalization {
  }
  
  // Prevents any two distinct endpoints from lying on a common horizontal line, and avoiding
  // the degenerate case. See Mark de Berg et al, Chapter 6.3
  //val SHEER = 0.0001f
  def shearTransform(point: Point) = Point(point.x, point.y + point.x * 0.0001f)
}
