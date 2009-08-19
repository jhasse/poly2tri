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

import shapes.{Point, Triangle, Segment}

// Advancing front
class AFront(iTriangle: Triangle) {

  // Doubly linked list
  var head = new Node(iTriangle.points(1), iTriangle)
  val middle = new Node(iTriangle.points(0), iTriangle)
  var tail = new Node(iTriangle.points(2), null)
  
  head.next = middle
  middle.next = tail
  middle.prev = head
  tail.prev = middle
  
  // TODO: Use Red-Black Tree or Interval Tree for better search performance!
  def locate(point: Point): Node = {
    var node = head
    while(node != tail) {
      if(point.x >= node.point.x && point.x < node.next.point.x)
        return node
      node = node.next
    }
    null
  }
  
  // Locate node containing given point
  def locatePoint(point: Point): Node = {
    var node = head
    while(node != null) {
      if(point == node.point)
        return node
      node = node.next
    }
    null
  }
  
  def insert(point: Point, triangle: Triangle, nNode: Node) = {
    val node = new Node(point, triangle)
    nNode.triangle = triangle
    nNode.next.prev = node
    node.next = nNode.next
    node.prev = nNode
    nNode.next = node
    node
  }
  
  def insertLegalized(point: Point, triangle: Triangle, nNode: Node) = {
    val node = new Node(triangle.points(1), triangle)
    val rNode = nNode.next
    rNode.prev = node
    node.next = rNode
    nNode.next = node
    node.prev = nNode
    node
  }
  
  // Update advancing front with constrained edge triangles
  def constrainedEdge(sNode: Node, eNode: Node, T1: ArrayBuffer[Triangle], 
                      T2: ArrayBuffer[Triangle], edge: Segment) {
    
    var node = sNode
    
    val point1 = edge.q
    val point2 = edge.p
    
    var marked = false
    
    // Scan the advancing front and update Node triangle pointers
    while(node != null && node != eNode) {
      
      T2.foreach(t => {
        if(t.contains(node.point, node.next.point))
          node.triangle = t
          marked = true
      })
      
      if(!marked)
        T1.foreach(t => {
          if(t.contains(node.point, node.next.point))
            node.triangle = t
        })

      node = node.next
    }
  }
  
  def -=(tuple: Tuple3[Node, Node, Triangle]) {
    val (node, kNode, triangle) = tuple
    kNode.next.prev = node
    node.next = kNode.next
    node.triangle = triangle
  }
  
  def link(node1: Node, node2: Node, t: Triangle) {
    node1.next = node2
    node2.prev = node1
    node1.triangle = t
  }
  
  // NOT IMPLEMENTED
  def basin(node: Node) {
    if(node.next != tail) {
      val p1 = node.point
      val p2 = node.next.point
      val slope = (p1.y - p2.y) / (p1.x - p2.x)
      if(slope < Math.Pi*3/4)
        println("basin slope = " + slope)
    }
    
  }
  
}

// Advancing front node
class Node(val point: Point, var triangle: Triangle) {
  var next: Node = null
  var prev: Node = null
}