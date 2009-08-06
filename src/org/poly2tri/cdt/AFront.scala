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

import shapes.{Point, Triangle}

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
    throw new Exception("Advancing front error: point not found")
  }
  
  def insert(tuple: Tuple3[Point, Triangle, Node]) = {
    val (point, triangle, nNode) = tuple
    val node = new Node(point, triangle)
    // Update pointer
    nNode.triangle = triangle
    // Insert new node into advancing front
    nNode.next.prev = node
    node.next = nNode.next
    node.prev = nNode
    nNode.next = node
    node
  }
  
  def -=(tuple: Tuple3[Node, Node, Triangle]) {
    val (node, kNode, triangle) = tuple
    kNode.next.prev = node
    node.next = kNode.next
    node.triangle = triangle
  }
  
}

// Advancing front node
class Node(val point: Point, var triangle: Triangle) {
  var next: Node = null
  var prev: Node = null
}