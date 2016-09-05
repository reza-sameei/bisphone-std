package com.bisphone.util.syntax

import com.bisphone.std._

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
final class StdEitherAccessors[T](val self: T) extends AnyVal {
  @inline def stdleft[R](implicit ev: T<:!<StdEither[_,_]): StdEither[T,R] = StdLeft(self)
  @inline def stdright[L](implicit ev: T<:!<StdEither[_,_]): StdEither[L,T] = StdRight(self)
}
