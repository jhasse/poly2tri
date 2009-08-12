
package org.poly2tri.utils

import scala.collection.mutable.ArrayBuffer

import shapes.Point

object Util {
  
  // Almost zero
  val COLLINEAR_SLOP = 0.1f
  
  val epsilon = exactinit
  val ccwerrboundA = (3.0 + 16.0 * epsilon) * epsilon

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

    val d = orient2d(p1, p2, p3)
    
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
  def orient(b: Point, a: Point, p: Point): Float = {
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
	
	   val errbound = Util.ccwerrboundA * detsum
	   if ((det >= errbound) || (-det >= errbound)) {
	     return det
	   } else {
	     // Cheat a little bit.... we have a degenerate triangle
	     val c = pc * 1.0001f
         return orient2d(pa, pb, c)
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
