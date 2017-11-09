package com.bisphone.util

import com.bisphone.std._
import scala.concurrent.ExecutionContextExecutor

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */

sealed trait ExtractError extends SimpleError

case class UnexpectedError(
    override val desc: String,
    override val cause: Option[Throwable]
) extends ExtractError

case class UndefinedKey(
    val name: String,
    override val desc: String,
    override val cause: Option[Throwable] = None
) extends ExtractError // Undefined Required Value

case class MissedValue(
    val name: String,
    override val desc: String,
    override val cause: Option[Throwable] = None
) extends ExtractError

case class EmptyValue(
    val name: String,
    override val desc: String,
    override val cause: Option[Throwable] = None
) extends ExtractError // Empty List

case class InvalidValue[T](
    val name: String,
    val origin: String,
    val convertor: Convertor[String, T],
    override val desc: String,
    override val cause: Option[Throwable] = None
) extends ExtractError

trait ValueExtractor {

    def optional[T](key: String)(
        implicit cnvt: Convertor[String, T],
        executor: ExecutionContextExecutor
    ): ValueExtractor.Result[Option[T]]

    def required[T](key: String)(
        implicit cnvt: Convertor[String, T],
        executor: ExecutionContextExecutor
    ): ValueExtractor.Result[T] =
        optional[T](key).flatMap { i: Option[T] =>
            i match {
                case None => UndefinedKey(key, s"Undefined key '${key}'").asyncLeft
                case Some(value) => value.asyncRight[ExtractError]
            }
        }

    def list[T](key: String)(
        implicit cntv: Convertor[String, T],
        executor: ExecutionContextExecutor
    ): ValueExtractor.Result[List[T]]

    def nelist[T](key: String)(
        implicit cntv: Convertor[String, T],
        executor: ExecutionContextExecutor
    ): ValueExtractor.Result[List[T]] =
        list[T](key) flatMap { v: List[T] =>
            v match {
                case Nil => UndefinedKey(key, s"Empty list for '${key}'").asyncLeft
                case list => list.asyncRight
            }
        }

}

object ValueExtractor {
    type Result[T] = AsyncResult[ExtractError, T]

    trait Syntax {

        def required[T](key: String)(
            implicit
            convertor: Convertor[String, T],
            executionContextExecutor: ExecutionContextExecutor,
            context: ValueExtractor
        ): Result[T] = context required key

        /*def required[T](
          ns: String,
          fn: Extractor[T]
        )(
          implicit context: ValueExtractor
        ): Result[T] = context required (ns, fn)*/

        def optional[T](key: String)(
            implicit
            convertor: Convertor[String, T],
            executionContextExecutor: ExecutionContextExecutor,
            context: ValueExtractor
        ): Result[Option[T]] = context optional key

        /*def optional[T](
          ns: String,
          fn: Extractor[T]
        )(
          implicit context: ValueExtractor
        ): Result[Option[T]] = context optional (ns, fn)*/

        def nelist[T](key: String)(
            implicit
            convertor: Convertor[String, T],
            executionContextExecutor: ExecutionContextExecutor,
            context: ValueExtractor
        ): Result[List[T]] = context nelist key

        /*def nelist[T](
          ns: String,
          fn: Extractor[T]
        )(
          implicit context: ValueExtractor
        ): Result[List[T]] = context nelist (ns, fn)*/

        def list[T](key: String)(
            implicit
            convertor: Convertor[String, T],
            executionContextExecutor: ExecutionContextExecutor,
            context: ValueExtractor
        ): Result[List[T]] = context list key

        /*def list[T](
          ns: String,
          fn: Extractor[T]
        )(
          implicit context: ValueExtractor
        ): Result[List[T]] = context list (ns, fn)*/

    }

    object Syntax extends Syntax

}

trait ArgumentExtractor extends ValueExtractor {

    def firstOption[T](
        implicit cnvt: Convertor[String, T],
        executor: ExecutionContextExecutor
    ): AsyncResult[ExtractError, Option[T]]

}

object ArgumentExtractor {

    trait Syntax extends ValueExtractor.Syntax {
        def firstOption[T](
            implicit
            convertor: Convertor[String, T],
            executionContextExecutor: ExecutionContextExecutor,
            context: ArgumentExtractor
        ): ValueExtractor.Result[Option[T]] = context.firstOption[T]
    }

    object Syntax extends Syntax

}



