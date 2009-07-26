package org.poly2tri.shapes

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
