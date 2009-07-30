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
object CDT {
  
  // Inital triangle factor
  val ALPHA = 0.3f
  
  // Triangulate simple polygon
  def init(points: ArrayBuffer[Point]): CDT = {
    
    var xmax, xmin = shearTransform(points.first).x
    var ymax, ymin = shearTransform(points.first).y
    
    // Calculate bounds
    for(i <- 0 until points.size) { 
      points(i) = shearTransform(points(i))
      val p = points(i)
      if(p.x > xmax) xmax = p.x
      if(p.x < xmin) xmin = p.x
      if(p.y > ymax) ymax = p.y
      if(p.y < ymin) ymin = p.y
    }
    
    val deltaX = ALPHA * (xmax - xmin)
    val deltaY = ALPHA * (ymax - ymin)
    val p1 = Point(xmin - deltaX, ymin - deltaY)
    val p2 = Point(xmax + deltaX, ymin - deltaY)
    
    val segments = initSegments(points)
    val sortedPoints = pointSort(points)
    val initialTriangle = new Triangle(Array(sortedPoints(0), p1, p2), null)
    new CDT(sortedPoints, segments, initialTriangle)
  }
  
    // Create segments and connect end points; update edge event pointer
  private def initSegments(points: ArrayBuffer[Point]): List[Segment] = {
    var segments = List[Segment]()
    for(i <- 0 until points.size-1) {
      val segment = new Segment(points(i), points(i+1))
      points(i+1).eEvent = segment
      segments = segment :: segments
    }
    val segment = new Segment(points.first, points.last)
    points.first.eEvent = segment
    segments =  segment :: segments
    segments
  }
  
  // Insertion sort is one of the fastest algorithms for sorting arrays containing 
  // fewer than ten elements, or for lists that are already mostly sorted.
  // Merge sort: O(n log n)
  private def pointSort(pts: ArrayBuffer[Point]): List[Point] = {
    if(pts.size < 10) 
      Util.insertSort((p1: Point, p2: Point) => p1 > p2)(pts).toList
    else
      Util.msort((p1: Point, p2: Point) => p1 > p2)(pts.toList)
  }
  
  // Prevents any two distinct endpoints from lying on a common horizontal line, and avoiding
  // the degenerate case. See Mark de Berg et al, Chapter 6.3
  //val SHEER = 0.0001f
  private def shearTransform(point: Point) = Point(point.x, point.y + point.x * 0.0001f)
  
}

class CDT(val points: List[Point], val segments: List[Segment], initialTriangle: Triangle) {
  
  // The triangle mesh
  val mesh = new Mesh(initialTriangle)
  // Advancing front
  val aFront = new AFront(initialTriangle)
  
  // Sweep points; build mesh
  sweep
  // Finalize triangulation
  finalization
  
  // Implement sweep-line paradigm
  private def sweep {
    
    for(i <- 1 until points.size) {
      val point = points(i)
      pointEvent(point)
      legalization
      edgeEvent(point)
    }
  }  
  
  // Point event
  private def pointEvent(point: Point) {
    
    // Neightbor points (ccw & cw) and triangle(i)
    val (nPts, nTri) = aFront.locate(point)
    val pts = Array(point, nPts(0),  nPts(1))
    val neighbors = Array(nTri, null, null)
    val triangle = new Triangle(pts, neighbors)
    mesh.map += triangle
  
    // Update neighbor's pointers
    if(nPts(0) == nTri.points(1) && nPts(1) == nTri.points(2)) 
      nTri.neighbors(2) = triangle 
    else if(nPts(0) == nTri.points(2) && nPts(1) == nTri.points(1))
      nTri.neighbors(1) = triangle
    else 
      throw new Exception("CDT Error!")
    
  }
  
  private def legalization {
    
  }
  
  private def legalizeEdge {
    
  }
  
  // EdgeEvent
  private def edgeEvent(point: Point) {
    
  }
  
  private def finalization {
  }
  
  def triangles = mesh.map
}
