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

import org.newdawn.slick.{BasicGame, GameContainer, Graphics, Color, AppGameContainer}
import org.newdawn.slick.geom.{Polygon, Circle}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

import seidel.Triangulator
import shapes.{Segment, Point, Triangle}
import earClip.EarClip
import cdt.CDT

// TODO: Lots of documentation!
//     : Add Hertel-Mehlhorn algorithm
object Poly2Tri {

  def main(args: Array[String]) {
	val container = new AppGameContainer(new Poly2TriDemo())
    container.setDisplayMode(800,600,false)
    container.start()
  }
  
}

class Poly2TriDemo extends BasicGame("Poly2Tri") {
  
  // Sedidel Triangulator
  var seidel: Triangulator = null
  var segments: ArrayBuffer[Segment] = null
  var chestSegs: ArrayBuffer[Segment] = null
  var headSegs: ArrayBuffer[Segment] = null
  
  // EarClip Triangulator
  val earClip = new EarClip
  var earClipResults: Array[poly2tri.earClip.Triangle] = null
  
  // Sweep Line Constraied Delauney Triangulator (CDT)
  var slCDT: CDT = null
  
  var polyX: ArrayBuffer[Float] = null
  var polyY: ArrayBuffer[Float] = null
    
  var quit = false
  var debug = false
  var drawMap = false
  var drawSegs = true
  var hiLighter = 0
  var drawEarClip = false
  var drawCDT = true
  var drawcdtMesh = false
  
  val nazcaMonkey = "data/nazca_monkey.dat"
  val nazcaHeron = "data/nazca_heron_old.dat"
  val bird = "data/bird.dat"
  val snake = "data/i.snake"
  val star = "data/star.dat"
  val strange = "data/strange.dat"
  val i18 = "data/i.18"
  val tank = "data/tank.dat"
  val dude = "data/dude.dat"
  
  var currentModel = dude
  var doCDT = true
  
  var mouseButton = 0
  var mousePressed = false
  var mouseDrag = false
  var mousePos = Point(0, 0)
  var mousePosOld = Point(0, 0)
  var deltaX = 0f
  var deltaY = 0f
  var scaleFactor = 0.85f
  
  var gameContainer: GameContainer = null
  
  def init(container: GameContainer) {
    gameContainer = container
    selectModel(currentModel)
  }
  
  def update(gc: GameContainer, delta: Int) {
    if(quit) gc exit
  }
  
  def render(container: GameContainer, g: Graphics) {
    
    g.drawString("'1-9' to cycle models, mouse to pan & zoom", 10, 520)
    g.drawString("'SPACE' to show Seidel debug info", 10, 532)
    g.drawString("'m' to show trapezoidal map (Seidel debug mode)", 10, 544)
    g.drawString("'e' to switch Seidel / EarClip", 10, 556)
    g.drawString("'d' to switch CDT / Seidel", 10, 568)
    g.drawString("'c' to show CDT mesh, 's' to draw edges", 10, 580)
    
    g.scale(scaleFactor, scaleFactor)
	g.translate(deltaX, deltaY)
   
    val red = new Color(1f, 0f,0.0f)
    val blue = new Color(0f, 0f, 1f)
    val green = new Color(0f, 1f, 0f)
    val yellow = new Color(1f, 1f, 0f)
    
   if(debug) {
	   val draw = if(drawMap) seidel.trapezoidMap else seidel.trapezoids
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
   
   if(!debug && !drawEarClip && !drawCDT) {
    var i = 0
    for(t <- seidel.polygons) {
        val poly = new Polygon
        t.foreach(p => poly.addPoint(p.x, p.y))
        g.setColor(red)
        g.draw(poly)
      }
   } else if (debug && drawMap && !drawEarClip){
    for(mp <- seidel.monoPolies) {
      val poly = new Polygon
      mp.foreach(p => poly.addPoint(p.x, p.y))
      g.setColor(yellow)
      g.draw(poly)
      }
    }
   
   if(drawEarClip) 
	   earClipResults.foreach(t => {
	      val triangle = new Polygon
	      triangle.addPoint(t.x(0), t.y(0))
	      triangle.addPoint(t.x(1), t.y(1))
	      triangle.addPoint(t.x(2), t.y(2))
	      g.setColor(red)
	      g.draw(triangle)
	    })
   
   if(drawCDT) {
     
       val draw = if(drawcdtMesh) slCDT.triangleMesh else slCDT.triangles
       
	   draw.foreach( t => {
	     if(false) {
		     for(i <- 0 to 2) {
		       val s = t.points(i)
	           val e = if(i == 2) t.points(0) else t.points(i + 1)
	           val j = if(i == 0) 2 else if(i == 1) 0 else 1
	           if(t.edges(j))
	             g.setColor(yellow)
	           else
	             g.setColor(red)
		       g.drawLine(s.x,s.y,e.x,e.y)
		     }
	     } else {
		     val triangle = new Polygon
			 triangle.addPoint(t.points(0).x, t.points(0).y)
			 triangle.addPoint(t.points(1).x, t.points(1).y)
			 triangle.addPoint(t.points(2).x, t.points(2).y)
			 g.setColor(red)
			 g.draw(triangle) 
         }
	   })
       
	   slCDT.debugTriangles.foreach( t => {
	     val triangle = new Polygon
		 triangle.addPoint(t.points(0).x, t.points(0).y)
		 triangle.addPoint(t.points(1).x, t.points(1).y)
		 triangle.addPoint(t.points(2).x, t.points(2).y)
		 g.setColor(blue)
		 g.draw(triangle) 
	   })
    
	   //slCDT.cList.foreach(c => {
       for(i <- 0 until slCDT.cList.size) {
	     val circ = new Circle(slCDT.cList(i).x, slCDT.cList(i).y, 0.5f)
	     g.setColor(blue); g.draw(circ); g.fill(circ)
       }               
       //})
       
   }
   
   if(drawSegs) {
     g.setColor(green)
     for(i <- 0 until segments.size) {
       val s = segments(i)
       g.drawLine(s.p.x,s.p.y,s.q.x,s.q.y)
     }
   }
   
   if(currentModel == "data/dude.dat" && drawSegs) {
     g.setColor(green)
     for(i <- 0 until chestSegs.size) {
       val s = chestSegs(i)
       g.drawLine(s.p.x,s.p.y,s.q.x,s.q.y)
     }
     for(i <- 0 until headSegs.size) {
       val s = headSegs(i)
       g.drawLine(s.p.x,s.p.y,s.q.x,s.q.y)
     }
   }
   
  }
  
  /**
   * Handle mouseDown events.
   * @param p The screen location that the mouse is down at.
   */
  override def mousePressed(b: Int, x: Int, y: Int) {
    mouseButton = b
    mousePressed = true
    mousePosOld = mousePos
    mousePos = Point(x, y)
    
    // Right click
    // Correctly adjust for pan and zoom
    if(mouseButton == 1) {
      val point = mousePos/scaleFactor + Point(deltaX, deltaY)
      slCDT.addPoint(point)
      slCDT.triangulate
    }
    
  }
  
  /**
   * Handle mouseUp events.
   */
  override def mouseReleased(b: Int, x: Int, y: Int) {
    mousePosOld = mousePos
    mousePos = Point(x,y)
    mousePressed = false
  }
  
  /**
   * Handle mouseMove events (TestbedMain also sends mouseDragged events here)
   * @param p The new mouse location (screen coordinates)
   */
  override def mouseMoved(oldX: Int, oldY: Int, x: Int, y: Int) {
    mousePosOld = mousePos
    mousePos = Point(x,y)
    if(mousePressed) {
	  deltaX += mousePos.x - mousePosOld.x
      deltaY += mousePos.y - mousePosOld.y
    }
  }
  
  override def mouseWheelMoved(notches: Int) {
    if (notches < 0) {
      scaleFactor = Math.min(300f, scaleFactor * 1.05f);
    }
    else if (notches > 0) {
      scaleFactor = Math.max(.02f, scaleFactor / 1.05f);
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
      if (hiLighter == seidel.polygons.size)
        hiLighter = 0
    }
    // DOWN
    if(key == 208) {
      hiLighter -= 1
      if (hiLighter == -1)
        hiLighter = seidel.polygons.size-1
    }
    
    if(c == 'm') drawMap = !drawMap 
    if(c == 'd') drawCDT = !drawCDT
    if(c == '1') selectModel(nazcaMonkey)
    if(c == '2') selectModel(bird)
    if(c == '3') selectModel(strange)
    if(c == '4') selectModel(snake)
    if(c == '5') selectModel(star)
    if(c == '6') selectModel(i18)
    if(c == '7') selectModel(nazcaHeron)
    if(c == '8') selectModel(tank)
    if(c == '9') selectModel(dude)
    if(c == 's') drawSegs = !drawSegs
    if(c == 'c') drawcdtMesh = !drawcdtMesh
    if(c == 'e') {drawEarClip = !drawEarClip; drawCDT = false; selectModel(currentModel)}
    if(c == 'r') slCDT.refine
    
  }
    
  def selectModel(model: String) {
    model match {
      case "data/nazca_monkey.dat" => 
        val clearPoint = Point(418, 282)
        loadModel(nazcaMonkey, 4.5f, Point(400, 300), 1500, clearPoint)
      case "data/bird.dat" => 
        val clearPoint = Point(400, 300)
        loadModel(bird, 25f, Point(400, 300), 350, clearPoint)
      case "data/i.snake" => 
        val clearPoint = Point(336f, 196f)
        loadModel(snake, 10f, Point(600, 300), 10, clearPoint)
      case "data/star.dat" => 
        val clearPoint = Point(400, 204)
        loadModel(star, -1f, Point(0f, 0f), 10, clearPoint)
      case "data/strange.dat" => 
        val clearPoint = Point(400, 268)
        loadModel(strange, -1f, Point(0f, 0f), 15, clearPoint)
      case "data/i.18" => 
        val clearPoint = Point(510, 385)
        loadModel(i18, 20f, Point(600f, 500f), 20, clearPoint)
      case "data/nazca_heron_old.dat" => 
        val clearPoint = Point(85, 290)
        loadModel(nazcaHeron, 4.2f, Point(400f, 300f), 1500, clearPoint) 
      case "data/tank.dat" => 
        val clearPoint = Point(450, 350)
        loadModel(tank, -1f, Point(100f, 0f), 10, clearPoint)
      case "data/dude.dat" => 
        val clearPoint = Point(365, 427)
        loadModel(dude, -1f, Point(100f, -200f), 10, clearPoint)
      case _ => 
        assert(false)
        
    }
    currentModel = model
  }
   
  def loadModel(model: String, scale: Float, center: Point, maxTriangles: Int, clearPoint: Point) {
    
    println
    println("************** " + model + " **************")
    
    polyX = new ArrayBuffer[Float]
    polyY = new ArrayBuffer[Float]
    var points = new ArrayBuffer[Point]
    
    val angle = Math.Pi
    for (line <- Source.fromFile(model).getLines) {
      val s = line.replaceAll("\n", "")
      val tokens = s.split("[ ]+")
      if(tokens.size == 2) {	
       var x = tokens(0).toFloat
       var y = tokens(1).toFloat
       // Transform the shape
       polyX += (Math.cos(angle)*x - Math.sin(angle)*y).toFloat * scale + center.x
       polyY += (Math.sin(angle)*x + Math.cos(angle)*y).toFloat * scale + center.y
       points += new Point(polyX.last, polyY.last)
      } else {
        throw new Exception("Bad input file")
      }
    }
    
    segments = new ArrayBuffer[Segment]
    for(i <- 0 until points.size-1)
      segments += new Segment(points(i), points(i+1))
    segments += new Segment(points.first, points.last)
    
    println("Number of points = " + polyX.size)
    println
    
    if(doCDT) {
      
        val pts = points.toArray
        
	    val t1 = System.nanoTime
	    slCDT = new CDT(pts, clearPoint)
     
        // Add some holes....
        if(model == "data/dude.dat") {
          
          val headHole = Array(Point(325f,437f), Point(320f,423f), Point(329f,413f), Point(332f,423f))
          val chestHole = Array(Point(320.72342f,480f), Point(338.90617f,465.96863f), 
                                Point(347.99754f,480.61584f), Point(329.8148f,510.41534f), 
                                Point(339.91632f,480.11077f), Point(334.86556f,478.09046f))
            
          // Tramsform the points 
          for(i <- 0 until headHole.size) {
            val hx = -headHole(i).x*scale + center.x
            val hy = -headHole(i).y*scale + center.y
            headHole(i) = Point(hx, hy)
           }
          for(i <- 0 until chestHole.size) {
             val cx = -chestHole(i).x*scale + center.x
             val cy = -chestHole(i).y*scale + center.y
             chestHole(i) = Point(cx, cy)
          }
          
          chestSegs = new ArrayBuffer[Segment]
	      for(i <- 0 until chestHole.size-1)
	        chestSegs += new Segment(chestHole(i), chestHole(i+1))
	      chestSegs += new Segment(chestHole.first, chestHole.last)
    
          headSegs = new ArrayBuffer[Segment]
	      for(i <- 0 until headHole.size-1)
	        headSegs += new Segment(headHole(i), headHole(i+1))
	      headSegs += new Segment(headHole.first, headHole.last)
       
          // Add the holes
          slCDT.addHole(headHole)
          slCDT.addHole(chestHole)
        }
        
        slCDT triangulate
	    val runTime = System.nanoTime - t1                             
        
	    println("CDT average (ms) =  " + runTime*1e-6)
		println("Number of triangles = " + slCDT.triangles.size)
	    println
    }
    
    if(!drawEarClip) {  
        
	    // Sediel triangulation
        val t1 = System.nanoTime
	    seidel = new Triangulator(points)
        val runTime = System.nanoTime - t1
	   
	    println("Poly2Tri average (ms) =  " + runTime*1e-6)
	    println("Number of triangles = " + seidel.polygons.size)
     
    } else {
      
    	// Earclip
	    
	    earClipResults = new Array[poly2tri.earClip.Triangle](maxTriangles)
     
	    for(i <- 0 until earClipResults.size) earClipResults(i) = new poly2tri.earClip.Triangle
     
	    var xVerts = polyX.toArray
	    var yVerts = polyY.toArray
	   
	    val t1 = System.nanoTime
	    earClip.triangulatePolygon(xVerts, yVerts, xVerts.size, earClipResults)
	    val runTime = System.nanoTime - t1
	
	    println
	    println("Earclip average (ms) =  " + runTime*1e-6) 
	    println("Number of triangles = " + earClip.numTriangles)
    }
  }
   
}