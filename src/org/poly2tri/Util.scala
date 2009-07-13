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

import collection.jcl.ArrayList

import org.villane.vecmath.Vector2

object Util {

  final def rotateLeft90(v:Vector2) = new Vector2( -v.y, v.x )

  final def rotateRight90(v:Vector2) = new Vector2(v.y, -v.x )

  final def rotate(v:Vector2, angle:Float) = {
    val cos = Math.cos(angle).asInstanceOf[Float]
    val sin = Math.sin(angle).asInstanceOf[Float]
    val u = new Vector2((cos * v.x) - (sin * v.y), (cos * v.y) + (sin * v.x))
    u
  }

  final def clamp(a: Float, low: Float, high: Float) =
    if (a < low) low
    else if (a > high) high
    else a
    
  final def left(a: Vector2, b: Vector2, c: Vector2) =
    ((b.x - a.x)*(c.y - a.y) - (c.x - a.x)*(b.y - a.y) > 0)

  /** Melkman's Algorithm
   *  www.ams.sunysb.edu/~jsbm/courses/345/melkman.pdf
   *  Return a convex hull in ccw order
   */
  def hull(V: Array[Vector2]) = {

    val n = V.length
    val D = new Array[Vector2](2 * n + 1)
    var bot = n - 2
    var top = bot + 3

    D(bot) = V(2)
    D(top) = V(2)

    if (left(V(0), V(1), V(2))) {
      D(bot+1) = V(0)
      D(bot+2) = V(1)
    } else {
      D(bot+1) = V(1)
      D(bot+2) = V(0)
    }

    var i = 3
    while(i < n) {
      while (left(D(bot), D(bot+1), V(i)) && left(D(top-1), D(top), V(i))) {
        i += 1
      }
      while (!left(D(top-1), D(top), V(i))) top -= 1
      top += 1; D(top) = V(i)
      while (!left(D(bot), D(bot+1), V(i))) bot += 1
      bot -= 1; D(bot) = V(i)
      i += 1
    }

    val H = new Array[Vector2](top - bot)
    var h = 0
    while(h < (top - bot)) {
      H(h) = D(bot + h)
      h += 1
    }
    H
  }

  def svgToWorld(points: Array[Float], scale: Float) = {
    val verts = new Array[Vector2](points.length/2)
    var i = 0
    while(i < verts.length) {
      verts(i) = worldPoint(points(i*2), points(i*2+1), scale)
      i += 1
    }
    verts
  }

  def worldPoint(x: Float, y: Float, scale: Float) = {
    val p = Vector2(x*scale, y*scale)
    p
  }

}

/** The object <code>Random</code> offers a default implementation
 *  of scala.util.Random and random-related convenience methods.
 *
 *  @since 2.8
 */
object Random extends scala.util.Random {
  
  /** Returns a new sequence in random order.
   *  @param  seq   the sequence to shuffle
   *  @return       the shuffled sequence
   */
  def shuffle[T](buf: ArrayList[T]): ArrayList[T] = {
    // It would be better if this preserved the shape of its container, but I have
    // again been defeated by the lack of higher-kinded type inference.  I can
    // only make it work that way if it's called like
    //   shuffle[Int,List](List.range(0,100))
    // which nicely defeats the "convenience" portion of "convenience method".
       
    def swap(i1: Int, i2: Int) {
      val tmp = buf(i1)
      buf(i1) = buf(i2)
      buf(i2) = tmp
    }
   
    for (n <- buf.length to 2 by -1) {
      val k = nextInt(n)
      swap(n - 1, k)
    }
    buf
  }
}
