package com.bisphone.stdv1.predef

import com.bisphone.stdv1.predef.TypeAndValue._

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
final class StdEitherAccessors[T](val self: T) extends AnyVal {
//    @inline def stdleft[R](implicit ev: T<:!<StdEither[_,_]): StdEither[T,R] = StdLeft(self)
//    @inline def stdright[L](implicit ev: T<:!<StdEither[_,_]): StdEither[L,T] = StdRight(self)

    @inline def stdleft[R]: StdEither[T,R] = StdLeft(self)
    @inline def stdright[L]: StdEither[L,T] = StdRight(self)
}

final class OptionAccessors[T](val self: T) extends AnyVal {
    @inline def optional(implicit ev: T<:!<Option[_]): Option[T] = Option(self)
    @inline def some(implicit ev: T<:!<Option[_]): Option[T] = Some(self)
}


final class StdFailureAccessors[T<:Throwable](val self: T) extends AnyVal {
    @inline def cause: Option[Throwable] = Option(self.getCause)
    @inline def subject: String = self.getMessage
    @inline def tryFailure[R]: StdTry[R] = StdFailure(self)
}

final class StdSuccessAccessors[T](val self: T) extends AnyVal {
    @inline def trySuccess(implicit ev: T <:!< Throwable): StdTry[T] = StdSuccess[T](self)
}

final class StdFutureAccessors[T](val self: T) extends AnyVal {
    @inline def successfulFuture(implicit ev: T <:!< Throwable): StdFuture[T] = StdFuture successful self
    // @inline def successfulFuture(implicit ev: T <:!< Throwable): StdFuture[T] = StdFuture successful self
    @inline def failedFuture[R](implicit ev: T <:< Throwable): StdFuture[R] = StdFuture failed self
}