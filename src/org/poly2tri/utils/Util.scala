
package org.poly2tri.utils

import scala.collection.mutable.ArrayBuffer

import shapes.Point

object Util {
  
  // Almost zero
  val COLLINEAR_SLOP = 0.1f
  
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
    
    // 3x2 matrix
    val a11 = p1.x
    val a12 = p1.y
    val a21 = p2.x
    val a22 = p2.y
    val a31 = p3.x
    val a32 = p3.y
    
    // Determinant
    val d = a11*(a22-a32) - a12*(a21-a31) + (a21*a32-a31*a22)

    if(d <= COLLINEAR_SLOP) 
      true
    else 
      false
    
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
