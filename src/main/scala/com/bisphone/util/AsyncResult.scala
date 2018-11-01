package com.bisphone.util

import com.bisphone.std._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future, Promise}
import scala.util.control.NonFatal

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
final class AsyncResult[+L, +R](
    val underlay: Future[StdEither[L, R]]
) extends AnyVal {
    def asFuture: Future[StdEither[L, R]] = underlay
}


final class AsyncResultOps[L, R](val self: AsyncResult[L, R]) {

    def sync(timeout: Duration) = Try {
        Await.result(self.asFuture, timeout)
    }


    def flatMap[R2](fn: R => AsyncResult[L, R2])(
        implicit ec: ExecutionContextExecutor
    ): AsyncResult[L, R2] = {

        val p = Promise[StdEither[L, R2]]

        self.underlay onComplete {
            case StdSuccess(StdRight(value)) =>
                try { p completeWith fn(value).asFuture } catch {
                    case cause: Throwable => p failure cause
                }

            case StdSuccess(StdLeft(error)) =>
                p success StdLeft(error)

            case StdFailure(cause) =>
                p failure cause
        }

        new AsyncResult(p.future)
    }

    def map[R2](fn: R => R2)(
        implicit ec: ExecutionContextExecutor
    ): AsyncResult[L, R2] = {
        val p = Promise[StdEither[L, R2]]
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

    def leftFlatMap[L2](fn: L => AsyncResult[L2, R])(
        implicit ec: ExecutionContextExecutor
    ): AsyncResult[L2, R] = {
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
        implicit ec: ExecutionContextExecutor
    ): AsyncResult[L2, R] = {
        val p = Promise[StdEither[L2, R]]
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

    def recover(fn: PartialFunction[Throwable, AsyncResult[L, R]])(
        implicit ec: ExecutionContextExecutor
    ): AsyncResult[L, R] = {
        val p = Promise[StdEither[L, R]]
        self.underlay onComplete {
            case StdSuccess(StdRight(value)) => p success StdRight(value)
            case StdSuccess(StdLeft(error)) => p success StdLeft(error)
            case StdFailure(NonFatal(cause)) if fn.isDefinedAt(cause) =>
                try {
                    p completeWith fn(cause).asFuture
                } catch {
                    case NonFatal(cause) => p failure cause
                }
            case StdFailure(cause) => p failure cause
        }
        new AsyncResult(p.future)
    }

    def swap(implicit ec: ExecutionContextExecutor): AsyncResult[R, L] =
        new AsyncResult(self.underlay map {
            _.swap
        }
        )

    def mergeAsFuture[T](fn: StdEither[L, R] => T)(
        implicit ex: ExecutionContextExecutor
    ): Future[T] = self.asFuture map fn

    def flatten[L2, R2](fn: L2 => L)(
        implicit ex: ExecutionContextExecutor,
        ev: R <:< AsyncResult[L2, R2]
    ): AsyncResult[L, R2] = {
        val promise = Promise[StdEither[L, R2]]
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
        ex: ExecutionContextExecutor,
        ev: R <:< AsyncResult[L, R2]
    ): AsyncResult[L, R2] = flatten[L, R2](i => i)

    def get(timeout: Duration): StdTry[StdEither[L, R]] =
        Try(Await.result(self.underlay, timeout))

}


object AsyncResult {

    implicit def syntax$asyncResult[L, R](s: AsyncResult[L, R]) = new AsyncResultOps(s)

    // By value/ref

    def lazyApply[L, R](fn: => AsyncResult[L, R]) = try {
        fn
    } catch {
        case NonFatal(cause) => AsyncResult fromFailure cause
    }

    def lazyAsync[L, R](fn: => StdEither[L, R]) = try {
        new AsyncResult(Future successful fn)
    } catch {
        case NonFatal(cause) => AsyncResult fromFailure cause
    }

    def fromFuture[L, R](ft: Future[StdEither[L, R]]): AsyncResult[L, R] = new AsyncResult(ft)

    def fromEither[L, R](e: StdEither[L, R]): AsyncResult[L, R] = fromFuture(Future successful e)

    def right[L, R](r: R): AsyncResult[L, R] = fromEither(StdRight(r))

    def left[L, R](l: L): AsyncResult[L, R] = fromEither(StdLeft(l))

    def fromFailure[L, R](cause: Throwable): AsyncResult[L, R] = fromFuture(Future failed cause)

    // By name

    def lazyFuture[L, R](fn: => Future[StdEither[L, R]]): AsyncResult[L, R] = fromFuture(fn)

    def lazyEither[L, R](fn: => StdEither[L, R]): AsyncResult[L, R] = fromEither(fn)

    def collectRight[L, R](list: Seq[AsyncResult[L, R]])(
        implicit ece: ExecutionContextExecutor
    ): AsyncResult[L, Seq[R]] = {

        val rsl = (Future sequence list.map(_.asFuture)) map { is =>

            val buf = scala.collection.mutable.Buffer.empty[R]

            is.foldLeft(buf.stdright[L]) { (buf, i) =>
                i match {
                    case StdLeft(value) => value.stdleft
                    case StdRight(value) => buf.map { b => b append value; b }
                }
            }.map(_.toList)
        }

        AsyncResult fromFuture rsl
    }

    def collectLeft[L, R](list: Seq[AsyncResult[L, R]])(
        implicit ece: ExecutionContextExecutor
    ): AsyncResult[Seq[L], R] = {

        val rsl = (Future sequence list.map(_.asFuture)) map { is =>

            val buf = scala.collection.mutable.Buffer.empty[L]

            is.foldLeft(buf.stdleft[R]) { (buf, i) =>
                i match {
                    case StdRight(value) => value.stdright
                    case StdLeft(value) => buf.leftMap { b => b append value; b }
                }
            }.leftMap(_.toList)
        }

        AsyncResult fromFuture rsl
    }
}

/**
  * Another way to implmentation: Using TypeTag with Call-By-Name ... How?!
  * `
  * import scala.reflect.runtime.universe._
  * def meth[A : TypeTag](xs: => List[A]) = typeOf[A] match {
  * case t if t =:= typeOf[String] => "list of strings"
  * case t if t <:< typeOf[Foo] => "list of foos"
  * }
  * `
  */
