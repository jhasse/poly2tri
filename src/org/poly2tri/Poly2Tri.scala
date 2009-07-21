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
  var segments: ArrayBuffer[Segment] = null
  val earClip = new EarClip
  
  var quit = false
  var debug = false
  var drawMap = false
  var drawSegs = true
  var hiLighter = 0
  
  def init(container: GameContainer) {
    poly
    earClipPoly
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
	     if(!drawMap) {
	       val lCirc = new Circle(t.leftPoint.x, t.leftPoint.y, 4)
	       g.setColor(blue); g.draw(lCirc); g.fill(lCirc)
	       val rCirc = new Circle(t.rightPoint.x+5, t.rightPoint.y, 4)
	       g.setColor(yellow); g.draw(rCirc); g.fill(rCirc)
         }                          
	     g.setColor(red)
	     g.draw(polygon) 
	    }
   }
   
   if(!debug) {
    var i = 0
    for(t <- tesselator.triangles) {
        val triangle = new Polygon
        t.foreach(p => triangle.addPoint(p.x, p.y))
        g.setColor(red)
        g.draw(triangle)
      }
   } else if (debug && drawMap){
    for(mp <- tesselator.monoPolies) {
      val poly = new Polygon
      mp.foreach(p => poly.addPoint(p.x, p.y))
      g.setColor(yellow)
      g.draw(poly)
      }
    }
   
   if(drawSegs) {
     g.setColor(green)
     for(s <- segments) 
         g.drawLine(s.p.x,s.p.y,s.q.x,s.q.y)
   }
   
    /*
   earClipResults.foreach(t => {
      val triangle = new Polygon
      triangle.addPoint(t.x(0), t.y(0))
      triangle.addPoint(t.x(1), t.y(1))
      triangle.addPoint(t.x(2), t.y(2))
      g.setColor(red)
      g.draw(triangle)
    })*/
   
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
    if(c == '1') {poly; earClipPoly; hiLighter = 0}
    if(c == '2') {snake; hiLighter = 0}
    if(c == '3') {star; hiLighter = 0}
    if(c == 's') drawSegs = !drawSegs

  }
  
  // Test #1
  def poly {
	
    val p1 = Point(400,472)
    val p2 = Point(500,392)
    val p3 = Point(520,272)
    val p4 = Point(460,232)
    val p5 = Point(580,212)
    val p6 = Point(480,152)
    val p7 = Point(360,172)
    val p8 = Point(360,52)
    val p9 = Point(300,112)
    val p10 = Point(200,32)
    val p11 = Point(120,92)
    val p12 = Point(200,72)
    val p13 = Point(340,272)
    val p14 = Point(208,212)
    val p15 = Point(180,352)
    val p16 = Point(300,312)
    
    segments = new ArrayBuffer[Segment]
    segments += new Segment(p16, p1) 
    segments += new Segment(p9, p10)
    segments += new Segment(p13, p14)
    segments += new Segment(p5, p6)
    segments += new Segment(p2, p3)
    segments += new Segment(p1, p2)
    segments += new Segment(p4, p5)
    segments += new Segment(p7, p8)
    segments += new Segment(p8, p9)
    segments += new Segment(p10, p11)
    segments += new Segment(p11, p12)
    segments += new Segment(p12, p13)
    segments += new Segment(p3, p4)
    segments += new Segment(p15, p16)
    segments += new Segment(p14, p15)
    segments += new Segment(p6, p7)
    
    tesselator = new Triangulator(segments)
    val t1 = System.nanoTime
    tesselator process
    val t2 = System.nanoTime
    println
    println("**Poly1**")
    println("Poly2Tri core (ms) = " + tesselator.coreTime*1e-6)
    println("Poly2Tri total (ms) = " + (t2-t1)*1e-6)

   }
  
  def star {
	
    val p1 = Point(350,75)
    val p2 = Point(379,161)
    val p3 = Point(469,161)
    val p4 = Point(397,215)
    val p5 = Point(423,301)
    val p6 = Point(350,250)
    val p7 = Point(277,301)
    val p8 = Point(303,215)
    val p9 = Point(231,161)
    val p10 = Point(321,161)
    
    segments = new ArrayBuffer[Segment]
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
    
    val t1 = System.nanoTime
    tesselator process
    val t2 = System.nanoTime
    println
    println("**Star**")
    println("Poly2Tri core (ms) = " + tesselator.coreTime*1e-6)
    println("Poly2Tri total (ms) = " + (t2-t1)*1e-6)
  }
  
  // Test #2
  def snake {
	
    val scale = 10.0f
    val displace = 100
    val p1 = Point(10,1)*scale+displace
    val p2 = Point(20,10)*scale+displace
    val p3 = Point(30,1)*scale+displace
    val p4 = Point(40,10)*scale+displace
    val p5 = Point(50,1)*scale+displace
    val p6 = Point(50,10)*scale+displace
    val p7 = Point(40,20)*scale+displace
    val p8 = Point(30,10)*scale+displace
    val p9 = Point(20,20)*scale+displace
    val p10 = Point(10,10)*scale+displace
    val p11 = Point(1,20)*scale+displace
    val p12 = Point(1,10)*scale+displace
    
    segments = new ArrayBuffer[Segment]
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
    
    val t1 = System.nanoTime
    tesselator process
    val t2 = System.nanoTime
    println
    println("**Snake**")
    println("Poly2Tri core (ms) = " + tesselator.coreTime*1e-6)
    println("Poly2Tri total (ms) = " + (t2-t1)*1e-6)

  }
  
  def earClipPoly {
    
    val polyX = Array(400f, 500f, 520f, 460f, 580f, 480f, 360f, 360f, 300f, 200f, 120f, 200f, 340f, 208f, 180f, 300f)
    val polyY = Array(472f, 392f, 272f, 232f, 212f, 152f, 172f, 52f, 112f, 32f, 92f, 72f, 272f, 212f, 352f, 312f)
    
    val earClipResults = new Array[Triangle](14)
    for(i <- 0 until earClipResults.size) earClipResults(i) = new Triangle
    val t1 = System.nanoTime
    earClip.triangulatePolygon(polyX, polyY, polyX.size, earClipResults)
    val t2 = System.nanoTime
    println("Earclip total (ms) =  " + (t2-t1)*1e-6)
  }
  
  def earClipSnake {
    
    val polyX = Array(200f, 300f, 400f, 500f, 600f, 600f, 500f, 400f, 300f, 200f, 110f, 110f)
    val polyY = Array(110f, 200f, 110f, 200f, 110f, 200f, 300f, 200f, 300f, 200f, 300f, 200f)
    
    val earClipResults = new Array[Triangle](14)
    for(i <- 0 until earClipResults.size) earClipResults(i) = new Triangle
    val t1 = System.nanoTime
    earClip.triangulatePolygon(polyX, polyY, polyX.size, earClipResults)
    val t2 = System.nanoTime
    println("Earclip total (ms) =  " + (t2-t1)*1e-6)
  }
  
  def earClipStar {
    
    val p1 = Point(350,75)
    val p2 = Point(379,161)
    val p3 = Point(469,161)
    val p4 = Point(397,215)
    val p5 = Point(423,301)
    val p6 = Point(350,250)
    val p7 = Point(277,301)
    val p8 = Point(303,215)
    val p9 = Point(231,161)
    val p10 = Point(321,161)
    
    val polyX = Array(350f, 379f, 469f, 397f, 423f, 350f, 277f, 303f, 231f, 321f)
    val polyY = Array(75f, 161f, 161f, 215f, 301f, 250f, 301f,215f, 161f, 161f)
    
    val earClipResults = new Array[Triangle](14)
    for(i <- 0 until earClipResults.size) earClipResults(i) = new Triangle
    val t1 = System.nanoTime
    earClip.triangulatePolygon(polyX, polyY, polyX.size, earClipResults)
    val t2 = System.nanoTime
    println("Earclip total (ms) =  " + (t2-t1)*1e-6)
  }
  
}