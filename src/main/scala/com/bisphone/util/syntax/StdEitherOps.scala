package com.bisphone.util.syntax

import com.bisphone.std._

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
final class StdEitherOps[L,R] (val self: Either[L,R]) extends AnyVal {

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
