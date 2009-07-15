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
import org.newdawn.slick.geom.{Polygon, Circle}

import scala.collection.mutable.ArrayBuffer

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
  var debug = false
  var drawMap = false
  var hiLighter = 0
  
  def init(container: GameContainer) {
    snake
  }
  
  def update(gc: GameContainer, delta: Int) {
    if(quit) gc exit
  }
  
  def render(container: GameContainer, g: Graphics) {
    
    val red = new Color(1f, 0f,0.0f)
    val blue = new Color(0f, 0f, 1f)
    val green = new Color(0f, 1f, 0f)
    val yellow = new Color(1f, 1f, 0f)
    
   if(debug) {
	   val draw = if(drawMap) tesselator.trapezoidMap else tesselator.trapezoids
	   for(t <- draw) {
	     val polygon = new Polygon()
	     for(v <- t.vertices) {
	       polygon.addPoint(v.x, v.y)
	     }
	     val lCirc = new Circle(t.leftPoint.x, t.leftPoint.y, 4)
	     g.setColor(blue); g.draw(lCirc); g.fill(lCirc)
	     val rCirc = new Circle(t.rightPoint.x, t.rightPoint.y, 6)
	     //g.setColor(yellow); g.draw(rCirc); g.fill(rCirc)
	     g.setColor(red)
	     g.draw(polygon)
	    }
   }
   
   if(!debug) {
    var i = 0
    for(t <- tesselator.triangles) {
        val triangle = new Polygon
        t.foreach(p => triangle.addPoint(p.x, p.y))
        val color = if(i == hiLighter) blue else red
        g.setColor(color)
        g.draw(triangle)
        i += 1
      }
   } else {
    for(mp <- tesselator.monoPolies) {
      val poly = new Polygon
      mp.foreach(p => poly.addPoint(p.x, p.y))
      g.setColor(yellow)
      g.draw(poly)
      }
    }
    
  }
  
  override def keyPressed(key:Int, c:Char) {
    // ESC
    if(key == 1) quit = true
    // SPACE
    if(key == 57) debug = !debug
    // UP
    if(key == 200) {
      hiLighter += 1
      if (hiLighter == tesselator.triangles.size)
        hiLighter = 0
    }
    // DOWN
    if(key == 208) {
      hiLighter -= 1
      if (hiLighter == -1)
        hiLighter = tesselator.triangles.size-1
    }
    if(c == 'm') drawMap = !drawMap
    if(c == '1') {poly; hiLighter = 0}
    if(c == '2') {snake; hiLighter = 0}
    if(c == '3') {star; hiLighter = 0}

  }
  
  // Test #1
  def poly {
   
    val scale = 1.0f
    val p1 = new Point(100,300)*scale
    val p2 = new Point(400,500)*scale
    val p3 = new Point(260,200)*scale
    val p4 = new Point(600,175)*scale
    val p5 = new Point(400,300)*scale
    val p6 = new Point(650,250)*scale
    
    val segments = new ArrayBuffer[Segment]
    segments += new Segment(p1, p2)
    segments += new Segment(p3, p4)
    segments += new Segment(p1, p3)
    segments += new Segment(p5, p2)
    segments += new Segment(p5, p6)
    segments += new Segment(p4, p6) 
    
    tesselator = new Triangulator(segments)
    tesselator.process
   }
  
  def star {
	
    val p1 = new Point(350,75)
    val p2 = new Point(379,161)
    val p3 = new Point(469,161)
    val p4 = new Point(397,215)
    val p5 = new Point(423,301)
    val p6 = new Point(350,250)
    val p7 = new Point(277,301)
    val p8 = new Point(303,215)
    val p9 = new Point(231,161)
    val p10 = new Point(321,161)
    
    val segments = new ArrayBuffer[Segment]
    segments += new Segment(p1, p2)
    segments += new Segment(p2, p3)
    segments += new Segment(p3, p4)
    segments += new Segment(p4, p5)
    segments += new Segment(p5, p6)
    segments += new Segment(p6, p7) 
    segments += new Segment(p7, p8)
    segments += new Segment(p8, p9)
    segments += new Segment(p9, p10)
    segments += new Segment(p10, p1)
    tesselator = new Triangulator(segments)
    tesselator.process
  }
  
  // Test #2
  def snake {

    val scale = 10.0f
    val displace = 100
    val p1 = new Point(10,1)*scale+displace
    val p2 = new Point(20,10)*scale+displace
    val p3 = new Point(30,1)*scale+displace
    val p4 = new Point(40,10)*scale+displace
    val p5 = new Point(50,1)*scale+displace
    val p6 = new Point(50,10)*scale+displace
    val p7 = new Point(40,20)*scale+displace
    val p8 = new Point(30,10)*scale+displace
    val p9 = new Point(20,20)*scale+displace
    val p10 = new Point(10,10)*scale+displace
    val p11 = new Point(1,20)*scale+displace
    val p12 = new Point(1,10)*scale+displace
    
    val segments = new ArrayBuffer[Segment]
    segments += new Segment(p1, p2)
    segments += new Segment(p2, p3)
    segments += new Segment(p3, p4)
    segments += new Segment(p4, p5)
    segments += new Segment(p5, p6)
    segments += new Segment(p6, p7) 
    segments += new Segment(p7, p8)
    segments += new Segment(p8, p9)
    segments += new Segment(p9, p10)
    segments += new Segment(p10, p11)
    segments += new Segment(p11, p12)
    segments += new Segment(p12, p1)
    tesselator = new Triangulator(segments)
    tesselator.process
  }
  
  
}