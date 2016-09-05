package com.bisphone.util.syntax

import scala.concurrent.{ExecutionContextExecutor, Future}
import com.bisphone.std._
import com.bisphone.util.AsyncResult

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
final class AsyncResultAccessors[T](val self: T) extends AnyVal {

  @inline def asyncRight[L]: AsyncResult[L,T] = AsyncResult.right(self)

  @inline def asyncLeft[R]: AsyncResult[T, R] = AsyncResult.left(self)

}
