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

// Based on Raimund Seidel's paper "A simple and fast incremental randomized
// algorithm for computing trapezoidal decompositions and for triangulating polygons"
// See also: "Computational Geometry", 3rd edition, by Mark de Berg et al, Chapter 6.2
//           "Computational Geometry in C", 2nd edition, by Joseph O'Rourke

import org.newdawn.slick.{BasicGame, GameContainer, Graphics, Color, AppGameContainer}
import org.newdawn.slick.geom.Polygon

import collection.jcl.ArrayList

// TODO: Lots of documentation!

object Poly2Tri {

  def main(args: Array[String]) {
	val container = new AppGameContainer(new Poly2TriDemo())
    container.setDisplayMode(800,600,false)
    container.start()
  }
  
}

class Poly2TriDemo extends BasicGame("Poly2Tri") {
  
  var tesselator: Triangulator = null
  var quit = false
  
  def init(container: GameContainer) {
    testTesselator
  }
  
  def update(gc: GameContainer, delta: Int) {
    if(quit) gc exit
  }
  
  def render(container: GameContainer, g: Graphics) {
    
    val red = new Color(1f,0.0f,0.0f)
    val blue = new Color(0f, 0f, 1f)
    val green = new Color(0f, 1f, 0f)
    
   //for(t <- tesselator.allTrapezoids) {
   for(t <- tesselator.trapezoids) {
     val polygon = new Polygon()
     for(v <- t.vertices) {
       polygon.addPoint(v.x, v.y)
     }
     //g.setColor(red)
     //g.draw(polygon)
    }
   
    for(x <- tesselator.xMonoPoly) {
      var t = x.triangles
      for(t <- x.triangles) { 
        val triangle = new Polygon()
        t.foreach(p => triangle.addPoint(p.x, p.y))
        g.setColor(green)
        g.draw(triangle)
      }
    }
  }
  
  override def keyPressed(key:Int, c:Char) {
    if(key == 1) quit = true
  }
  
  def testTesselator {
   
    val scale = 1.0f
    val p1 = new Point(100,300)*scale
    val p2 = new Point(400,500)*scale
    val p3 = new Point(260,200)*scale
    val p4 = new Point(600,175)*scale
    val p5 = new Point(400,300)*scale
    val p6 = new Point(650,250)*scale
    
    val segments = new ArrayList[Segment]
    segments += new Segment(p1, p2)
    segments += new Segment(p3, p4)
    segments += new Segment(p1, p3)
    segments += new Segment(p5, p2)
    segments += new Segment(p5, p6)
    segments += new Segment(p4, p6) 
    
    tesselator = new Triangulator(segments)
    tesselator.process
   }
  
  
}