package com.bisphone.util.syntax

import com.bisphone.std._

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
class StdTryOps[T](val self: StdTry[T]) extends AnyVal {

  def failMap( f: Throwable => Throwable ): StdTry[T] = self match {
    case StdFailure(cause) => StdFailure(f(cause))
    case success => success
  }

  def failMap(msg: String): StdTry[T] = failMap(e => new scala.RuntimeException(msg,e))
}
