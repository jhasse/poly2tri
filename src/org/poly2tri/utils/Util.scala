
package org.poly2tri.utils

import scala.collection.mutable.ArrayBuffer

import shapes.Point

object Util {
  
  // Almost zero
  val COLLINEAR_SLOP = 0.1f
  
  val epsilon = exactinit
  val ccwerrboundA = (3.0 + 16.0 * epsilon) * epsilon
  val iccerrboundA = (10.0 + 96.0 * epsilon) * epsilon
  
  // From "Scala By Example," by Martin Odersky
  def msort[A](less: (A, A) => Boolean)(xs: List[A]): List[A] = {
    def merge(xs1: List[A], xs2: List[A]): List[A] =
      if (xs1.isEmpty) xs2
      else if (xs2.isEmpty) xs1
      else if (less(xs1.head, xs2.head)) xs1.head :: merge(xs1.tail, xs2)
      else xs2.head :: merge(xs1, xs2.tail)
      val n = xs.length/2
      if (n == 0) xs
      else merge(msort(less)(xs take n), msort(less)(xs drop n))
  }
  
  def insertSort[A](less: (A, A) => Boolean)(xs: ArrayBuffer[A]): ArrayBuffer[A] = {    
    var j = 1
    while(j < xs.size){   
	   val key = xs(j)
	   var i = j-1
	   while(i >= 0 && less(key, xs(i)) ){
	     xs(i+1) = xs(i)
	     i -= 1
	   }
	   xs(i+1)=key
	   j += 1
	 }
	 xs
  }
  
  // Tests if the given points are collinear
  def collinear(p1: Point, p2: Point, p3: Point): Boolean = {

    val d = Math.abs((p2-p1) cross (p1-p3))
    
    if(Math.abs(d) <= COLLINEAR_SLOP) 
      true
    else 
      false
    
  }
  
  /* From Jonathan Shewchuk's "Adaptive Precision Floating-Point Arithmetic 
   * and Fast Robust Predicates for Computational Geometry"
   * See: http://www.cs.cmu.edu/~quake/robust.html
   */
  def exactinit = {
  
	  var every_other = true
	  var half = 0.5
      var splitter = 1.0
	  var epsilon = 1.0
	  var check = 1.0
	  var lastcheck = 0.0
   
	  do {
	    lastcheck = check
	    epsilon *= half
	    if (every_other) {
	      splitter *= 2.0
	    }
	    every_other = !every_other
	    check = 1.0 + epsilon
	  } while ((check != 1.0) && (check != lastcheck))

	  epsilon
  
  }
  
  // Approximate 2D orientation test.  Nonrobust. 
  // Return: positive if point  a, b, and c are counterclockwise
  //         negative if point a, b, and c are clockwise
  //         zero if points are collinear
  // See: http://www-2.cs.cmu.edu/~quake/robust.html
  def orient(a: Point, b: Point, p: Point): Float = {
    val acx = a.x - p.x
    val bcx = b.x - p.x
    val acy = a.y - p.y
    val bcy = b.y - p.y
    acx * bcy - acy * bcx  
  }
   
   // Adaptive exact 2D orientation test.  Robust. By Jonathan Shewchuk
   // Return: positive if point a, b, and c are counterclockwise
   //         negative if point a, b, and c are clockwise
   //         zero if points are collinear
   // See: http://www-2.cs.cmu.edu/~quake/robust.html
   def orient2d(pa: Point, pb: Point, pc: Point): Double = {
  
	   val detleft: Double = (pa.x - pc.x) * (pb.y - pc.y)
	   val detright: Double = (pa.y - pc.y) * (pb.x - pc.x)
	   val det = detleft - detright
	   var detsum = 0.0
	  
	   if (detleft > 0.0) {
	     if (detright <= 0.0) {
	       return det;
	     } else {
	       detsum = detleft + detright
	     }
	   } else if (detleft < 0.0) {
	     if (detright >= 0.0) {
	       return det
	     } else {
	       detsum = -detleft - detright
	     }
	   } else {
	     return det
	   }
	
	   val errbound = ccwerrboundA * detsum
	   if ((det >= errbound) || (-det >= errbound)) {
	     return det
	   } else {    
	     // Cheat a little bit.... we have a degenerate triangle
	     val c = pc * 0.1e-6f
         return orient2d(pa, pb, c)
	   }

   }
   
   // Returns triangle circumcircle point and radius
   def circumCircle(a: Point, b: Point, c: Point): Tuple2[Point, Float] = {
     
     val A = det(a, b, c)
     val C = detC(a, b, c)
     
     val bx1 = Point(a.x*a.x + a.y*a.y, a.y)
     val bx2 = Point(b.x*b.x + b.y*b.y, b.y)
     val bx3 = Point(c.x*c.x + c.y*c.y, c.y)
     val bx = det(bx1, bx2, bx3)
     
     val by1 = Point(a.x*a.x + a.y*a.y, a.x)
     val by2 = Point(b.x*b.x + b.y*b.y, b.x)
     val by3 = Point(c.x*c.x + c.y*c.y, c.x)
     val by = det(by1, by2, by3)
     
     val x = bx / (2*A)
     val y = by / (2*A)
     
     val center = Point(x, y)
     val radius = Math.sqrt(bx*bx + by*by - 4*A*C).toFloat / (2*Math.abs(A))
     
     (center, radius)
   }
   
   def det(p1: Point, p2: Point, p3: Point): Float = {
     
     val a11 = p1.x
     val a12 = p1.y
     val a13,a23,a33 = 1f
     val a21 = p2.x
     val a22 = p2.y
     val a31 = p3.x
     val a32 = p3.y
     
     a11*(a22*a33-a23*a32) - a12*(a21*a33 - a23*a31) + a13*(a21*a32-a22*a31)
     
   }
   
   def detC(p1: Point, p2: Point, p3: Point): Float = {
     
     val a11 = p1.x*p1.x + p1.y*p1.y
     val a12 = p1.x
     val a13 = p1.y
     val a21 = p2.x*p2.x + p2.y*p2.y
     val a22 = p2.x
     val a23 = p2.y
     val a31 = p3.x*p3.x + p3.y*p3.y
     val a32 = p3.x
     val a33 = p3.y
     
     a11*(a22*a33-a23*a32) - a12*(a21*a33 - a23*a31) + a13*(a21*a32-a22*a31)
     
   }
   
   /* Approximate 2D incircle test.  Nonrobust.  By Jonathan Shewchuk
    * Return a positive value if the point pd lies inside the     
    * circle passing through pa, pb, and pc; a negative value if  
    * it lies outside; and zero if the four points are cocircular.
    * The points pa, pb, and pc must be in counterclockwise       
    * order, or the sign of the result will be reversed.   
   */
   def incirclefast(pa: Point, pb: Point, pc: Point, pd: Point): Boolean = {
     
    val adx = pa.x - pd.x
    val ady = pa.y - pd.y
    val bdx = pb.x - pd.x
    val bdy = pb.y - pd.y
    val cdx = pc.x - pd.x
    val cdy = pc.y - pd.y

    val abdet = adx * bdy - bdx * ady
    val bcdet = bdx * cdy - cdx * bdy
    val cadet = cdx * ady - adx * cdy
    val alift = adx * adx + ady * ady
    val blift = bdx * bdx + bdy * bdy
    val clift = cdx * cdx + cdy * cdy

    alift * bcdet + blift * cadet + clift * abdet >= 0
    
  }

   /* Robust 2D incircle test, modified. Original By Jonathan Shewchuk
    * Return a positive value if the point pd lies inside the     
    * circle passing through pa, pb, and pc; a negative value if  
    * it lies outside; and zero if the four points are cocircular.
    * The points pa, pb, and pc must be in counterclockwise       
    * order, or the sign of the result will be reversed.   
   */
   def incircle(pa: Point, pb: Point, pc: Point, pd: Point): Boolean = {
  
     val adx = pa.x - pd.x
     val bdx = pb.x - pd.x
     val cdx = pc.x - pd.x
     val ady = pa.y - pd.y
     val bdy = pb.y - pd.y
     val cdy = pc.y - pd.y

     val bdxcdy = bdx * cdy
     val cdxbdy = cdx * bdy
     val alift = adx * adx + ady * ady

     val cdxady = cdx * ady
     val adxcdy = adx * cdy
     val blift = bdx * bdx + bdy * bdy

     val adxbdy = adx * bdy
     val bdxady = bdx * ady
     val clift = cdx * cdx + cdy * cdy

     val det = alift * (bdxcdy - cdxbdy) + 
               blift * (cdxady - adxcdy) + 
               clift * (adxbdy - bdxady)

     val permanent = (Math.abs(bdxcdy) + Math.abs(cdxbdy)) * alift + 
                     (Math.abs(cdxady) + Math.abs(adxcdy)) * blift + 
                     (Math.abs(adxbdy) + Math.abs(bdxady)) * clift
     
     val errbound = iccerrboundA * permanent
     
     if ((det > errbound) || (-det > errbound)) {
       return det >= 0
     } else {
       throw new Exception("Points nearly collinear")
     }

}

 
}

/** The object <code>Random</code> offers a default implementation
 *  of scala.util.Random and random-related convenience methods.
 *
 *  @since 2.8
 *  From Scala 2.8 standard library
 */
object Random extends scala.util.Random {
  
  /** Returns a new sequence in random order.
   *  @param  seq   the sequence to shuffle
   *  @return       the shuffled sequence
   */
  def shuffle[T](buf: ArrayBuffer[T]): ArrayBuffer[T] = {
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
