package com.bisphone.util

import scala.collection.mutable

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
case class Iter[T](list: Seq[T], iterationToken: Option[String])

object Iter {
   def apply[T](origin: Seq[T], limit: Int)(fn: T => String): Iter[T] = {
      if (origin.size <= limit) Iter(origin, None) else {
         var n = limit
         var ptr = origin
         var last = ptr.head
         val list = mutable.ListBuffer.empty[T]
         while(n > 0) {
            n -= 1
            last = ptr.head
            ptr = ptr.tail
            list += last
         }
         Iter(list.toList, Some(fn(last)))
      }
   }
}
