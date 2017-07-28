package com.bisphone.stdv1.util

import scala.concurrent.{Await, ExecutionContext, Promise}
import scala.util.control.NonFatal
import com.bisphone.stdv1.predef.TypeAndValue._
import com.bisphone.stdv1.predef.Syntax._

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */

final class AsyncResult[+L, +R](
    val underlay: StdFuture[StdEither[L,R]]
) {
    def asFuture: StdFuture[StdEither[L,R]] = underlay
}

object AsyncResult {

    implicit class Operations[L,R](val self: AsyncResult[L,R]) extends AnyVal {

        def get(timeout: Duration): StdTry[StdEither[L,R]] =
            Try(Await.result(self.underlay, timeout))

        def future[T](
            fn: StdTry[StdEither[L,R]] => T
        )(
            implicit ec: ExecutionContext
        ): StdFuture[T] = {
            val promise = Promise[T]()
            self.underlay.onComplete {
                case x =>
                    try { promise success fn(x) } catch {
                        case NonFatal(cause) => promise failure cause
                    }
            }
            promise.future
        }

        def flatFuture[T](
            fn: StdTry[StdEither[L,R]] => StdFuture[T]
        )(
            implicit ec: ExecutionContext
        ): StdFuture[T] = {
            val promise = Promise[T]()
            self.underlay.onComplete {
                case x =>
                    try { promise completeWith fn(x) } catch {
                        case NonFatal(cause) => promise failure cause
                    }
            }
            promise.future
        }

        def mapEither[L2,R2](
            fn: StdEither[L,R] => StdEither[L2,R2]
        )(
            implicit ec: ExecutionContext
        ): AsyncResult[L2,R2] = {
            AsyncResult fromFuture self.underlay.map(fn)
        }

        def flatTransform[L2,R2](
            fn: StdTry[StdEither[L,R]] => AsyncResult[L2,R2]
        )(
            implicit ec: ExecutionContext
        ): AsyncResult[L2,R2] = {
            val promise = Promise[StdEither[L2,R2]]()
            self.underlay.onComplete {
                case x =>
                    try { promise completeWith fn(x).asFuture } catch {
                        case NonFatal(cause) => promise failure cause
                    }
            }
            AsyncResult fromFuture promise.future
        }

        def transform[L2,R2](
            fn: StdTry[StdEither[L,R]] => StdTry[StdEither[L2,R2]]
        )(
            implicit ec: ExecutionContext
        ): AsyncResult[L2,R2] = {
            val promise = Promise[StdEither[L2,R2]]()
            self.underlay.onComplete {
                case x =>
                    try { promise complete fn(x) } catch {
                        case NonFatal(cause) => promise failure cause
                    }
            }
            AsyncResult fromFuture promise.future
        }

        /*def flatMap[R2](fn: R => AsyncResult[L,R2])(
            implicit ec: ExecutionContext
        ): AsyncResult[L,R2] = self.transform {
            case StdSuccess(StdRight(value)) => fn(value)
            case StdSuccess(StdLeft(value)) => AsyncResult left value
            case StdFailure(cause) => AsyncResult fromFailure cause
        }*/

        def flatMap[R2](fn: R => AsyncResult[L,R2])(
            implicit ec: ExecutionContext
        ): AsyncResult[L,R2] = {
            val p = Promise[StdEither[L,R2]]
            self.underlay onComplete {
                case StdSuccess(StdRight(value)) =>
                    try {
                        p completeWith fn(value).asFuture
                    } catch {
                        case NonFatal(cause) => p failure cause
                    }
                case StdSuccess(StdLeft(error)) => p success StdLeft(error)
                case StdFailure(cause) => p failure cause
            }
            new AsyncResult(p.future)
        }

        def map[R2](fn: R => R2)(
            implicit ec: ExecutionContext
        ): AsyncResult[L,R2] = {
            val p = Promise[StdEither[L,R2]]
            self.underlay onComplete {
                case StdSuccess(StdRight(value)) =>
                    try {
                        p success StdRight(fn(value))
                    } catch {
                        case NonFatal(cause) => p failure cause
                    }
                case StdSuccess(StdLeft(error)) => p success StdLeft(error)
                case StdFailure(cause) => p failure cause
            }
            new AsyncResult(p.future)
        }

        def leftFlatMap[L2](fn: L => AsyncResult[L2,R])(
            implicit ec: ExecutionContext
        ): AsyncResult[L2,R] = {
            val p = Promise[StdEither[L2, R]]
            self.underlay onComplete {
                case StdSuccess(StdRight(value)) => p success StdRight(value)
                case StdSuccess(StdLeft(error)) =>
                    try {
                        p completeWith fn(error).asFuture
                    } catch {
                        case NonFatal(cause) => p failure cause
                    }
                case StdFailure(cause) => p failure cause
            }
            new AsyncResult(p.future)
        }

        def leftMap[L2](fn: L => L2)(
            implicit ec: ExecutionContext
        ): AsyncResult[L2,R] = {
            val p = Promise[StdEither[L2,R]]
            self.underlay onComplete {
                case StdSuccess(StdRight(value)) => p success StdRight(value)
                case StdSuccess(StdLeft(error)) =>
                    try {
                        p success StdLeft(fn(error))
                    } catch {
                        case NonFatal(cause) => p failure cause
                    }
                case StdFailure(cause) => p failure cause
            }
            new AsyncResult(p.future)
        }

        def recover(fn: PartialFunction[Throwable, AsyncResult[L,R]])(
            implicit ec: ExecutionContext
        ): AsyncResult[L,R] = {
            val p = Promise[StdEither[L,R]]
            self.underlay onComplete {
                case StdSuccess(StdRight(value)) => p success StdRight(value)
                case StdSuccess(StdLeft(error)) => p success StdLeft(error)
                case StdFailure(NonFatal(cause)) if fn.isDefinedAt(cause) =>
                    try {
                        p completeWith fn(cause).asFuture
                    } catch {
                        case NonFatal(finalCause) => p failure finalCause
                    }
                case StdFailure(cause) => p failure cause
            }
            new AsyncResult(p.future)
        }

        def swap(implicit ec: ExecutionContext): AsyncResult[R,L] =
            new AsyncResult(self.underlay map {_.swap})

        def mergeAsFuture[T](fn: StdEither[L,R] => T)(
            implicit ec: ExecutionContext
        ): StdFuture[T] = self.asFuture map fn

        def flatten[L2, R2](fn:L2 => L)(
            implicit
            ec: ExecutionContext,
            ev: R <:< AsyncResult[L2,R2]
        ):AsyncResult[L, R2] = {
            val promise = Promise[StdEither[L,R2]]
            self.underlay onComplete {
                case StdSuccess(StdRight(value)) => value.asFuture.onComplete {
                    case StdSuccess(StdRight(r)) => promise success StdRight(r)
                    case StdSuccess(StdLeft(l)) => promise success StdLeft(fn(l))
                    case StdFailure(cause) => promise failure cause
                }
                case StdSuccess(StdLeft(error)) => promise success StdLeft(error)
                case StdFailure(cause) => promise failure cause
            }
            new AsyncResult(promise.future)
        }

        def flatten[R2](
            implicit
            ec: ExecutionContext,
            ev: R <:< AsyncResult[L,R2]
        ):AsyncResult[L, R2] = flatten[L,R2](i => i)

    }

    // By value/ref

    def fromFuture[L,R](ft: StdFuture[StdEither[L,R]]): AsyncResult[L,R] = new AsyncResult(ft)

    def fromEither[L,R](e: StdEither[L,R]): AsyncResult[L,R] = fromFuture(StdFuture successful e)

    def right[L,R](r: R): AsyncResult[L,R] = fromEither(StdRight(r))

    def left[L,R](l: L): AsyncResult[L, R] = fromEither(StdLeft(l))

    def fromFailure[L,R](cause: Throwable): AsyncResult[L,R] = fromFuture(StdFuture failed cause)

    // By name

    def lazyFuture[L,R](fn: => StdFuture[StdEither[L,R]]): AsyncResult[L,R] = fromFuture(fn)

    def lazyEither[L,R](fn: => StdEither[L,R]): AsyncResult[L,R] = fromEither(fn)

    // Utils

    def collectRight[L,R](list: Seq[AsyncResult[L,R]])(
        implicit
        ec: ExecutionContext
    ): AsyncResult[L, Seq[R]] = {

        val rsl = (StdFuture sequence list.map(_.asFuture)) map { is =>

            type Buf = scala.collection.mutable.Buffer[R]
            val buf = scala.collection.mutable.Buffer.empty[R]
            val tmp: StdEither[L,Buf] = StdRight(buf)

            // compiler can't find any way to prove Buf is not a subtype/supertype/equal of StdEither !!!
            /*is.foldLeft(buf.stdright[L]) { (buf, i) => i match {
                case StdLeft(value) => value.stdleft
                case StdRight(value) => buf.map{ b => b append value; b}
            }}.map(_.toList)*/

            is.foldLeft(tmp) { (buf, i) => i match {
                case StdLeft(value) => StdLeft(value)
                case StdRight(value) => buf.map{ b => b append value; b}
            }}.map(_.toList)
        }

        AsyncResult fromFuture rsl
    }

    def collectLeft[L,R](list: Seq[AsyncResult[L,R]])(
        implicit
        ec: ExecutionContext
    ): AsyncResult[Seq[L], R] = {

        val rsl = (StdFuture sequence list.map(_.asFuture)) map { is =>

            type Buf = scala.collection.mutable.Buffer[L]
            val buf = scala.collection.mutable.Buffer.empty[L]
            val tmp: StdEither[Buf,R] = StdLeft(buf)

            is.foldLeft(tmp) { (buf, i) => i match {
                case StdRight(value) => StdRight(value)
                case StdLeft(value) => buf.leftMap{ b => b append value; b }
            }}.leftMap(_.toList)
        }

        AsyncResult fromFuture rsl
    }

    /**
      * Another way to implmentation: Using TypeTag with Call-By-Name ... How?!
      * `
      * import scala.reflect.runtime.universe._
      * def meth[A : TypeTag](xs: => List[A]) = typeOf[A] match {
      *   case t if t =:= typeOf[String] => "list of strings"
      *   case t if t <:< typeOf[Foo] => "list of foos"
      * }
      * `
      */

}
