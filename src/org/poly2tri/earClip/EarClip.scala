/**
 * Ported from jBox2D. Original author: ewjordan 
 * Triangulates a polygon using simple ear-clipping algorithm. Returns
 * size of Triangle array unless the polygon can't be triangulated.
 * This should only happen if the polygon self-intersects,
 * though it will not _always_ return null for a bad polygon - it is the
 * caller's responsibility to check for self-intersection, and if it
 * doesn't, it should at least check that the return value is non-null
 * before using. You're warned!
 *
 * Triangles may be degenerate, especially if you have identical points
 * in the input to the algorithm.  Check this before you use them.
 *
 * This is totally unoptimized, so for large polygons it should not be part
 * of the simulation loop.
 *
 * Returns:
 * -1 if algorithm fails (self-intersection most likely)
 * 0 if there are not enough vertices to triangulate anything.
 * Number of triangles if triangulation was successful.
 *
 * results will be filled with results - ear clipping always creates vNum - 2
 * or fewer (due to pinch point polygon snipping), so allocate an array of
 * this size.
 */
package org.poly2tri.earClip

import shapes.Point
import utils.Util

class EarClip {

	val tol = .001f
	var hasPinchPoint = false
	var pinchIndexA = -1
	var pinchIndexB = -1
	var pin: Poly = null
 
	var numTriangles = 0
 
	def triangulatePolygon(x: Array[Float], y: Array[Float], vn: Int, results: Array[Triangle]): Int = {
			
	        val p1 = Point(x(0), y(0))
            val p2 = Point(x(1), y(1))
            val p3 = Point(x(2), y(2))
            
            val ccw = Util.orient2d(p1, p2, p3) > 0
            
            val xv = if(ccw) x else x.reverse.toArray
            val yv = if(ccw) y else y.reverse.toArray
            
	        if (vn < 3) return 0
	        var vNum = vn
         
			//Recurse and split on pinch points
			val pA = new Poly
			val pB = new Poly
			pin = new Poly(xv, yv, vNum)
			if (resolvePinchPoint(pin,pA,pB)){
				val mergeA = new Array[Triangle](pA.nVertices)
				val mergeB = new Array[Triangle](pB.nVertices)
				for (i <- 0 until pA.nVertices) {
					mergeA(i) = new Triangle();
				}
				for (i <- 0 until pB.nVertices) {
					mergeB(i) = new Triangle();
				}
				val nA = triangulatePolygon(pA.x,pA.y,pA.nVertices,mergeA)
				val nB = triangulatePolygon(pB.x,pB.y,pB.nVertices,mergeB)
				if (nA == -1 || nB == -1){
				    numTriangles = -1
					return numTriangles
				}
				for (i <- 0 until nA){
					results(i).set(mergeA(i));
				}
				for (i <- 0 until nB){
					results(nA+i).set(mergeB(i));
				}
				numTriangles = (nA+nB)
				return numTriangles
			}

	        val buffer = new Array[Triangle](vNum-2);
	        for (i <- 0 until buffer.size) {
	        	buffer(i) = new Triangle();
	        }
	        var bufferSize = 0;
	        var xrem = new Array[Float](vNum)
	        var yrem = new Array[Float](vNum)
	        for (i <- 0 until vNum) {
	            xrem(i) = xv(i);
	            yrem(i) = yv(i);
	        }
			
			val xremLength = vNum;
			
	        while (vNum > 3) {
	        	//System.out.println("vNum: "+vNum);
	            // Find an ear
	            var earIndex = -1;
				var earMaxMinCross = -1000.0f;
	            for (i <- 0 until vNum) {
	                if (isEar(i, xrem, yrem, vNum)) {
						val lower = remainder(i-1,vNum);
						val upper = remainder(i+1,vNum);
						var d1 = Point(xrem(upper)-xrem(i),yrem(upper)-yrem(i));
						var d2 = Point(xrem(i)-xrem(lower),yrem(i)-yrem(lower));
						var d3 = Point(xrem(lower)-xrem(upper),yrem(lower)-yrem(upper));

						d1 = d1.normalize
						d2 = d2.normalize
						d3 = d3.normalize
						val cross12 = Math.abs( d1 cross d2 )
						val cross23 = Math.abs( d2 cross d3 )
						val cross31 = Math.abs( d3 cross d1 )
						//Find the maximum minimum angle
						val minCross = Math.min(cross12, Math.min(cross23,cross31))
						if (minCross > earMaxMinCross){
							earIndex = i
							earMaxMinCross = minCross
						}
	                }
	            }
				
	            // If we still haven't found an ear, we're screwed.
	            // Note: sometimes this is happening because the
				// remaining points are collinear.  Really these
				// should just be thrown out without halting triangulation.
				if (earIndex == -1){
						
					System.out.println("Couldn't find an ear, dumping remaining poly:\n");
					System.out.println("Please submit this dump to ewjordan at Box2d forums\n");
					for (i <- 0 until bufferSize) {
						results(i).set(buffer(i));
					}
			
					if (bufferSize > 0) return bufferSize;
	                else {
	                  numTriangles = -1
	                  return numTriangles
                   }
				}
				
	            // Clip off the ear:
	            // - remove the ear tip from the list

	            vNum -= 1;
	            val newx = new Array[Float](vNum)
	            val newy = new Array[Float](vNum)
	            var currDest = 0;
	            for (i <- 0 until vNum) {
	                if (currDest == earIndex) currDest += 1
	                newx(i) = xrem(currDest);
	                newy(i) = yrem(currDest);
	                currDest += 1;
	            }
				
	            // - add the clipped triangle to the triangle list
	            val under = if(earIndex == 0) (vNum) else (earIndex - 1)
	            val over = if(earIndex == vNum) 0 else (earIndex + 1)
	            val toAdd = new Triangle(xrem(earIndex), yrem(earIndex), xrem(over), yrem(over), xrem(under), yrem(under));
	            buffer(bufferSize) = toAdd
	            bufferSize += 1;
				
	            // - replace the old list with the new one
	            xrem = newx;
	            yrem = newy;
	        }
			
	        val toAdd = new Triangle(xrem(1), yrem(1), xrem(2), yrem(2), xrem(0), yrem(0))
	        buffer(bufferSize) = toAdd;
	        bufferSize += 1;
			
	        assert(bufferSize == xremLength-2)
			
	        for (i <- 0 until bufferSize) {
	            results(i).set(buffer(i))
	        }
			numTriangles = bufferSize
	        return numTriangles
	}
 
	/**
	 * Finds and fixes "pinch points," points where two polygon
	 * vertices are at the same point.
	 *
	 * If a pinch point is found, pin is broken up into poutA and poutB
	 * and true is returned; otherwise, returns false.
	 *
	 * Mostly for internal use.
	 * 
	 * O(N^2) time, which sucks...
	 */
	private def resolvePinchPoint(pin: Poly, poutA: Poly, poutB: Poly): Boolean = {
	  
		if (pin.nVertices < 3) return false
		hasPinchPoint = false
		pinchIndexA = -1
		pinchIndexB = -1
		pinchIndex
		if (hasPinchPoint){
			val sizeA = pinchIndexB - pinchIndexA;
			if (sizeA == pin.nVertices) return false;
			val xA = new Array[Float](sizeA);
			val yA = new Array[Float](sizeA);
			for (i <- 0 until sizeA){
				val ind = remainder(pinchIndexA+i,pin.nVertices);
				xA(i) = pin.x(ind);
				yA(i) = pin.y(ind);
			}
			val tempA = new Poly(xA,yA,sizeA);
			poutA.set(tempA);
			
			val sizeB = pin.nVertices - sizeA;
			val xB = new Array[Float](sizeB);
			val yB = new Array[Float](sizeB);
			for (i <- 0 until sizeB){
				val ind = remainder(pinchIndexB+i,pin.nVertices);
				xB(i) = pin.x(ind);
				yB(i) = pin.y(ind);
			}
			val tempB = new Poly(xB,yB,sizeB);
			poutB.set(tempB);
		}
		return hasPinchPoint;
	}
 
	 //Fix for obnoxious behavior for the % operator for negative numbers...
	private def remainder(x: Int, modulus: Int): Int = {
		var rem = x % modulus
		while (rem < 0){
			rem += modulus
		}
		return rem
	}
 
	 def pinchIndex: Boolean = {
	   for (i <- 0 until pin.nVertices) {
	     if(!hasPinchPoint) {
			for (j <- i+1 until pin.nVertices){
				//Don't worry about pinch points where the points
				//are actually just dupe neighbors
				if (Math.abs(pin.x(i)-pin.x(j))<tol&&Math.abs(pin.y(i)-pin.y(j))<tol&&j!=i+1){
					pinchIndexA = i
					pinchIndexB = j
					hasPinchPoint = true
					return false
				}
			}
		  }
	   } 
       return true
	 }
  
	 /**
		 * Checks if vertex i is the tip of an ear in polygon defined by xv[] and
	     * yv[].
		 *
		 * Assumes clockwise orientation of polygon...ick
	     */
	private def isEar(i: Int , xv: Array[Float], yv: Array[Float], xvLength: Int): Boolean = {
	  
	        var dx0, dy0, dx1, dy1 = 0f
	        if (i >= xvLength || i < 0 || xvLength < 3) {
	            return false;
	        }
	        var upper = i + 1
	        var lower = i - 1
	        if (i == 0) {
	            dx0 = xv(0) - xv(xvLength - 1)
	            dy0 = yv(0) - yv(xvLength - 1)
	            dx1 = xv(1) - xv(0)
	            dy1 = yv(1) - yv(0)
	            lower = xvLength - 1
	        }
	        else if (i == xvLength - 1) {
	            dx0 = xv(i) - xv(i - 1);
	            dy0 = yv(i) - yv(i - 1);
	            dx1 = xv(0) - xv(i);
	            dy1 = yv(0) - yv(i);
	            upper = 0;
	        }
	        else {
	            dx0 = xv(i) - xv(i - 1);
	            dy0 = yv(i) - yv(i - 1);
	            dx1 = xv(i + 1) - xv(i);
	            dy1 = yv(i + 1) - yv(i);
	        }
	        val cross = dx0 * dy1 - dx1 * dy0;
	        if (cross > 0)
	            return false;
	        val myTri = new Triangle(xv(i), yv(i), xv(upper), yv(upper), xv(lower), yv(lower))
	        for (j <- 0 until xvLength) {
	            if (!(j == i || j == lower || j == upper)) {
	            if (myTri.containsPoint(xv(j), yv(j)))
	                return false;
	            }
	        }
	        return true;
	}
 
}

class Poly(var x: Array[Float], var y: Array[Float], var nVertices: Int) {
  
	var areaIsSet = false
	var area = 0f
 
	def this(_x: Array[Float], _y: Array[Float]) = this(_x,_y,_x.size)
	def this() = this(null, null, 0)
	
	def set(p: Poly ) {
	    if (nVertices != p.nVertices){
			nVertices = p.nVertices;
			x = new Array[Float](nVertices)
			y = new Array[Float](nVertices)
	    }
			
	    for (i <- 0 until nVertices) {
	        x(i) = p.x(i)
	        y(i) = p.y(i)
	    }
		areaIsSet = false
	}
}

class Triangle(var x1: Float, var y1: Float, var x2: Float, var y2: Float, var x3: Float, var y3: Float) {
    
	def this() = this(0,0,0,0,0,0)
 
    val x = new Array[Float](3)
    val y = new Array[Float](3)
    
    // Automatically fixes orientation to ccw

	  val dx1 = x2-x1
	  val dx2 = x3-x1
	  val dy1 = y2-y1
	  val dy2 = y3-y1
	  val cross = dx1*dy2-dx2*dy1
	  val ccw = (cross>0)
	  if (ccw){
	    x(0) = x1; x(1) = x2; x(2) = x3;
	    y(0) = y1; y(1) = y2; y(2) = y3;
	  } else{
	    x(0) = x1; x(1) = x3; x(2) = x2;
	    y(0) = y1; y(1) = y3; y(2) = y2;
	  }

    def set(t: Triangle) {
    	x(0) = t.x(0)
    	x(1) = t.x(1)
    	x(2) = t.x(2)
    	y(0) = t.y(0)
    	y(1) = t.y(1)
    	y(2) = t.y(2)
    }

    
    def containsPoint(_x: Float, _y: Float): Boolean = {
      
      val vx2 = _x-x(0); val vy2 = _y-y(0);
      val vx1 = x(1)-x(0); val vy1 = y(1)-y(0);
      val vx0 = x(2)-x(0); val vy0 = y(2)-y(0);
      
      val dot00 = vx0*vx0+vy0*vy0;
      val dot01 = vx0*vx1+vy0*vy1;
      val dot02 = vx0*vx2+vy0*vy2;
      val dot11 = vx1*vx1+vy1*vy1;
      val dot12 = vx1*vx2+vy1*vy2;
      val invDenom = 1.0f / (dot00*dot11 - dot01*dot01);
      val u = (dot11*dot02 - dot01*dot12)*invDenom;
      val v = (dot00*dot12 - dot01*dot02)*invDenom;
      
      return ((u>=0)&&(v>=0)&&(u+v<=1));    
    }
    
 }