package com.bisphone.util.syntax

import com.bisphone.std._

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
final class StdFailureAccessors[T<:Throwable](val self: T) extends AnyVal {
  @inline def cause: Option[Throwable] = Option(self.getCause)
  @inline def subject: String = self.getMessage
  @inline def tryFailure[R]: StdTry[R] = StdFailure(self)
}