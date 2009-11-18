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

import scala.collection.mutable.{ArrayBuffer, Queue}

import shapes.Point

// Doubly linked list
class MonotoneMountain {

	var tail, head: Point = null
	var size = 0

	private val convexPoints = new ArrayBuffer[Point]
    // Monotone mountain points
	val monoPoly = new ArrayBuffer[Point]
    // Triangles that constitute the mountain
	val triangles = new ArrayBuffer[Array[Point]]
	// Convex polygons that constitute the mountain
	val convexPolies = new ArrayBuffer[Array[Point]]
	// Used to track which side of the line we are on                                
	private var positive = false
	// Almost Pi!
	private val PI_SLOP = 3.1
 
	// Append a point to the list
	def +=(point: Point) {
	  size match {
	    case 0 => 
	      head = point
	      size += 1
      case 1 =>
        // Keep repeat points out of the list
        if(point ! head) {
	        tail = point
	        tail.prev = head
	        head.next = tail
	        size += 1
        }
      case _ =>
        // Keep repeat points out of the list
        if(point ! tail) {
	        tail.next = point
	        point.prev = tail
	        tail = point
	        size += 1
        }
	  }
	}

	// Remove a point from the list
	def remove(point: Point) {
		val next = point.next
		val prev = point.prev
		point.prev.next = next
		point.next.prev = prev
		size -= 1
	}
 
	// Partition a x-monotone mountain into triangles O(n)
	// See "Computational Geometry in C", 2nd edition, by Joseph O'Rourke, page 52
	def process {
	
	  // Establish the proper sign
	  positive = angleSign
	  // create monotone polygon - for dubug purposes
	  genMonoPoly
   
      // Initialize internal angles at each nonbase vertex
      // Link strictly convex vertices into a list, ignore reflex vertices
      var p = head.next
      while(p != tail) {
        val a = angle(p)
        // If the point is almost colinear with it's neighbor, remove it!
        if(a >= PI_SLOP || a <= -PI_SLOP || a == 0.0)
          remove(p)
        else if(convex(p))
          convexPoints += p
        p = p.next
      }
      
      triangulate

    }
 
	private def triangulate {
	  
      while(!convexPoints.isEmpty) {
	     
        val ear = convexPoints.remove(0)
        val a = ear.prev
        val b = ear
        val c = ear.next
        val triangle = Array(a, b, c)
      
        triangles += triangle
	     
        // Remove ear, update angles and convex list
        remove(ear)
        if(valid(a)) convexPoints += a
        if(valid(c)) convexPoints += c
      }
      assert(size <= 3, "Triangulation bug, please report")
   
	}

	private def valid(p: Point) = (p != head && p != tail && convex(p))
	  
	// Create the monotone polygon 
	private def genMonoPoly { 
      var p = head
	  while(p != null) {
        monoPoly += p
        p = p.next
	  }
	}
 
	private def angle(p: Point) = {
	  val a = (p.next - p)
	  val b = (p.prev - p)
	  Math.atan2(a cross b, a dot b)
	}
 
	private def angleSign = {
	  val a = (head.next - head)
	  val b = (tail - head)
	  (Math.atan2(a cross b, a dot b) >= 0)
	}
 
	// Determines if the inslide angle is convex or reflex
	private def convex(p: Point) = {
	  if(positive != (angle(p) >= 0)) false
      else true
  }

}
