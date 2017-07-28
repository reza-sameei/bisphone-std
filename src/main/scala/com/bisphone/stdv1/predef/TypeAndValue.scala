package com.bisphone.stdv1.predef

import scala.annotation.implicitNotFound

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
trait TypeAndValue {

    /** A way to prevent subtype params base on: https://gist.github.com/milessabin/c9f8befa932d98dcc7a4 */

    // Encoding for "A is not a subtype of B"
    // @implicitNotFound("WHAT")
    // could not find implicit value for parameter ev: com.bisphone.stdv1.predef.<:!<[scala.collection.mutable.Buffer[R],com.bisphone.stdv1.predef.StdEither[_, _]]
    @implicitNotFound("Need evidence to prove that ${A} is not a subtype of ${B} (${A} <:!< ${B})")
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

    val unit = ()

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

    type StdFuture[T] = scala.concurrent.Future[T]
    val StdFuture = scala.concurrent.Future

    type ExecutionContext = scala.concurrent.ExecutionContext
    val ExecutionContext = scala.concurrent.ExecutionContext

    type Duration = scala.concurrent.duration.Duration
    val Duration = scala.concurrent.duration.Duration
    type FiniteDuration = scala.concurrent.duration.FiniteDuration

    def none[T]: Option[T] = None


    type Logger = com.typesafe.scalalogging.Logger
    val Logger = com.typesafe.scalalogging.Logger
}

object TypeAndValue extends TypeAndValue
