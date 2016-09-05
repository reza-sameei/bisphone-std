package com.bisphone.util.syntax

import com.bisphone.std._

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
final class StdSuccessAccessors[T](val self: T) extends AnyVal {
  @inline def trySuccess(implicit ev: T <:!< Throwable): StdTry[T] = StdSuccess[T](self)
}