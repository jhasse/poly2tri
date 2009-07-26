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

import shapes.{Segment, Trapezoid}

// Directed Acyclic graph (DAG)
// See "Computational Geometry", 3rd edition, by Mark de Berg et al, Chapter 6.2
                           
class QueryGraph(var head: Node) {

  def locate(s: Segment) = head.locate(s).trapezoid
  
  def followSegment(s: Segment) = {
    
    val trapezoids = new ArrayBuffer[Trapezoid]
    trapezoids += locate(s)
    var j = 0
    while(s.q.x > trapezoids(j).rightPoint.x) {
      if(s > trapezoids(j).rightPoint) {
        trapezoids += trapezoids(j).upperRight
      } else {
        trapezoids += trapezoids(j).lowerRight
      }
      j += 1
    }
    trapezoids
  }
  
  def replace(sink: Sink, node: Node) {
    if(sink.parentList.size == 0) {
      head = node
    } else {
      node replace sink
    }
  }
  
  def case1(sink: Sink, s: Segment, tList: Array[Trapezoid]) {
    val yNode = new YNode(s, Sink.init(tList(1)), Sink.init(tList(2)))
    val qNode = new XNode(s.q, yNode, Sink.init(tList(3)))
	val pNode = new XNode(s.p, Sink.init(tList(0)), qNode)
    replace(sink, pNode)
  }
  
  def case2(sink: Sink, s: Segment, tList: Array[Trapezoid]) {
    val yNode = new YNode(s, Sink.init(tList(1)), Sink.init(tList(2)))
	val pNode = new XNode(s.p, Sink.init(tList(0)), yNode)
    replace(sink, pNode)
  }
  
  def case3(sink: Sink, s: Segment, tList: Array[Trapezoid]) {
    val yNode = new YNode(s, Sink.init(tList(0)), Sink.init(tList(1)))
    replace(sink, yNode)
  }
  
  def case4(sink: Sink, s: Segment, tList: Array[Trapezoid]) {
    val yNode = new YNode(s, Sink.init(tList(0)), Sink.init(tList(1)))
    val qNode = new XNode(s.q, yNode, Sink.init(tList(2)))
    replace(sink, qNode)
  }
  
}
