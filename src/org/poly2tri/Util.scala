
package org.poly2tri

import collection.jcl.ArrayList

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
