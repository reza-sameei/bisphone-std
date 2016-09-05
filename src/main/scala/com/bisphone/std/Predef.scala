package com.bisphone.std

import com.bisphone.util.{AsyncResult, AsyncResultOps, Convertors}

import scala.language.implicitConversions
import scala.util.control.NonFatal
import scala.concurrent.Future

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
trait Predef extends Convertors {

  /** A way to prevent subtype params base on: https://gist.github.com/milessabin/c9f8befa932d98dcc7a4 */

  // Encoding for "A is not a subtype of B"
  trait <:!<[A, B]

  // Uses ambiguity to rule out the cases we're trying to exclude
  implicit def notSubType[A, B] : A <:!< B = null
  implicit def notSubtype1[A, B >: A] : A <:!< B = null
  implicit def notSubType2[A, B >: A] : A <:!< B = null

  // Type alias for context bound
  type |¬|[T] = {
    type λ[U] = U <:!< T
  }

  // Types =============================================

  type StdTry[T] = scala.util.Try[T]
  val Try = scala.util.Try

  type StdSuccess[T] = scala.util.Success[T]
  val StdSuccess = scala.util.Success

  type StdFailure[T] = scala.util.Failure[T]
  val StdFailure = scala.util.Failure

  type StdEither[+L,+R] = scala.util.Either[L,R]
  val StdEither = scala.util.Either

  type StdRight[+L,+R] = scala.util.Right[L,R]
  val StdRight = scala.util.Right

  type StdLeft[+L,+R] = scala.util.Left[L,R]
  val StdLeft = scala.util.Left

  // Accessros =============================================
  implicit def toStdFailureAccessors[T<:Throwable](value: T) = new com.bisphone.util.syntax.StdFailureAccessors(value)
  implicit def toStdSuccessAccessors[T](value: T) = new com.bisphone.util.syntax.StdSuccessAccessors(value)
  implicit def toEitherAccessors[T](value: T) = new com.bisphone.util.syntax.StdEitherAccessors(value)
  implicit def toAsyncResultAccessors[T](value: T)(
    implicit ev1: T <:!< StdEither[_,_],
    ev2: T <:!< Future[_]
  ) = new com.bisphone.util.syntax.AsyncResultAccessors(value)

  // Ops =============================================
  implicit def toStdTryOps[T](value: StdTry[T]) = new com.bisphone.util.syntax.StdTryOps(value)
  implicit def toStdEitherOps[L,R](value: Either[L,R]) = new com.bisphone.util.syntax.StdEitherOps(value)
  implicit def toAsyncResultOps[L,R](value: AsyncResult[L,R]) = new AsyncResultOps(value)

  // Functions

  @inline final def catchNonFatal[T](f: => T): StdEither[Throwable,T] = try {
    StdRight(f)
  } catch {
    case NonFatal(cause:Throwable) => StdLeft(cause)
  }

}
