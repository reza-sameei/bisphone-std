package com.bisphone.stdv1.predef

import com.bisphone.stdv1.predef.TypeAndValue._

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */


final class StdEitherOperations[L,R] (val self: Either[L,R]) extends AnyVal {
    @inline def flatMap[R2](f: R => StdEither[L,R2]): StdEither[L,R2] = self match {
        case StdLeft(left) => StdLeft(left)
        case StdRight(right) => f(right)
    }
    @inline def map[R2](f: R => R2): StdEither[L,R2] = flatMap( r => StdRight(f(r)) )
    @inline def leftFlatMap[L2](f: L => Either[L2,R]): StdEither[L2,R] = self match {
        case StdLeft(left) => f(left)
        case StdRight(right) => StdRight(right)
    }
    @inline def leftMap[L2](f: L => L2): StdEither[L2,R] = leftFlatMap( l => StdLeft(f(l)) )
}

final class StdTryOps[T](val self: StdTry[T]) extends AnyVal {
    @inline def failMap( f: Throwable => Throwable ): StdTry[T] = self match {
        case StdFailure(cause) => StdFailure(f(cause))
        case success => success
    }
    @inline def failMap(msg: String): StdTry[T] = failMap(e => new scala.RuntimeException(msg,e))
}

