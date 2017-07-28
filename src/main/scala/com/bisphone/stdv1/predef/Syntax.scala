package com.bisphone.stdv1.predef

import com.bisphone.stdv1.predef.TypeAndValue._

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
trait Syntax {

    implicit def std$accessors$either[T](v: T) = new StdEitherAccessors(v)

    implicit def std$accessors$option[T](v: T) = new OptionAccessors(v)

    implicit def std$accessors$failure[T <: Throwable](v: T) = new StdFailureAccessors(v)

    implicit def std$accessors$success[T](v: T) = new StdSuccessAccessors(v)

    implicit def std$accessors$future[T](v: T) = new StdFutureAccessors(v)

    implicit def std$operations$either[L,R](v: StdEither[L,R]) = new StdEitherOperations(v)

    implicit def std$operations$try[T](v: StdFailure[T]) = new StdTryOps(v)

}

object Syntax extends Syntax

