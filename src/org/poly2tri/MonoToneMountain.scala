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

import scala.collection.mutable.{ArrayBuffer, Queue}

// Doubly linked list
class MonotoneMountain {

	var tail, head: Point = null
	var size = 0

	val convexPoints = new Queue[Point]
    // Monotone mountain points
	val monoPoly = new ArrayBuffer[Point]
    // Triangles that constitute the mountain
	val triangles = new ArrayBuffer[Array[Point]]
	// Used to track which side of the line we are on                                
	var positive = false
	// Almost Pi!
	val SLOP = 3.1
 
	// Append a point to the list
	def +=(point: Point) {
	  size match {
	    case 0 => 
	      head = point
        case 1 =>
          tail = point
		  tail.prev = head
		  head.next = tail
        case _ =>
          tail.next = point
          point.prev = tail
          tail = point
	  }
	  size += 1
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
	def triangulate {
	
	  // create monotone polygon - for dubug purposes
	  genMonoPoly
   
	  if(size == 3) {
	    lastTriangle
	  } else {
	   // Initialize internal angles at each nonbase vertex
	   // Link strictly convex vertices into a list, ignore reflex vertices
	   var p = head.next
	   while(p != tail) {
	     val a = angle(p)
         // If the point is almost colinear with it's neighbor, remove it!
	     if(a >= SLOP || a <= -SLOP) 
           remove(p)
	     else 
	       if(convex(p)) convexPoints.enqueue(p)
	     p = p.next
	   }
    
	   while(!convexPoints.isEmpty) {
	     
	     val ear = convexPoints.dequeue
	     val a = ear.prev.clone
	     val b = ear.clone
	     val c = ear.next.clone
	     val triangle = Array(a, b, c)
      
	     triangles += triangle
	     
	     // Remove ear, update angles and convex list
	     remove(ear) 
	     if(a.prev != null && convex(a)) convexPoints.enqueue(a); 
         if(c.prev != null && convex(c)) convexPoints.enqueue(c)

	   }    
	   assert(size <= 3, "Triangulation bug")
	   if(size == 3)lastTriangle
	}
   }
 
	// Create the monotone polygon 
	private def genMonoPoly { 
      var p = head
	  while(p != null) {
	      monoPoly += p
	      p = p.next
	  }
	}
 
	def angle(p: Point) = {
	  val a = (p.next - p)
	  val b = (p.prev - p)
	  Math.atan2(a cross b, a dot b)
	}
 
	// Determines if the inslide angle is convex or reflex
	private def convex(p: Point) = {
	  val cvx = (angle(p) >= 0)
	  if(p.prev == head) 
	    positive = cvx
	  if(positive != cvx)
        false
      else
        true
    }

	private def lastTriangle {
	  val triangle = new Array[Point](3)
	  var i = 0
      var p = head
	  while(p != null) {
	      triangle(i) = p
	      p = p.next
          i += 1
	  }
	  triangles += triangle
	}
}
