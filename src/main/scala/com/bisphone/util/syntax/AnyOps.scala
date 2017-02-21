package com.bisphone.util.syntax

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
class AnyOps[T](val self: T) extends AnyVal {

    @inline def optional: Option[T] = Option(self)

    @inline def some: Option[T] = Option(self)

}
