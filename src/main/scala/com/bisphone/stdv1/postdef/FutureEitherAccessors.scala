package com.bisphone.stdv1.postdef

import com.bisphone.stdv1.predef._
import com.bisphone.stdv1.util.AsyncResult

/**
  */

final class FutureEitherAccessors[L,R](val self: StdFuture[StdEither[L,R]]) extends AnyVal {
    @inline def toAsyncResult: AsyncResult[L,R] = AsyncResult fromFuture self

}

final class FutureAccessors[T](val self: StdFuture[T]) extends AnyVal {

    @inline def leftAsyncResult[R](implicit executionContext: ExecutionContext): AsyncResult[T,R] = AsyncResult fromFuture self.map(_.stdleft)
    @inline def rightAsyncResult[L](implicit executionContext: ExecutionContext): AsyncResult[L, T] = AsyncResult fromFuture self.map(_.stdright)

    @inline def asyncLeft[R](implicit executionContext: ExecutionContext): AsyncResult[T,R] = AsyncResult fromFuture self.map(_.stdleft)
    @inline def asyncRight[L](implicit executionContext: ExecutionContext): AsyncResult[L, T] = AsyncResult fromFuture self.map(_.stdright)
}

final class AnyAccessors[T](val self: T) extends AnyVal {
    @inline def asyncLeft[R](implicit executionContext: ExecutionContext): AsyncResult[T,R] = AsyncResult left self
    @inline def asyncRight[L](implicit executionContext: ExecutionContext): AsyncResult[L, T] = AsyncResult right self
}