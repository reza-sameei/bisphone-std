package com.bisphone.stdv1.util

import scala.concurrent.ExecutionContext
import com.bisphone.stdv1.predef._

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */

trait ValueExtractor {

    import ValueExtractor.Result

    def required[T](key: String)(
        implicit convertor: Convertor[String, T]
    ): Result[T]

    def optional[T](key: String)(
        implicit convertor: Convertor[String, T]
    ): Result[Option[T]]

    def nelist[T](key: String)(
        implicit convertor: Convertor[String, T]
    ): Result[List[T]]

    def list[T](key: String)(
        implicit convertor: Convertor[String, T]
    ): Result[List[T]]

    def required[T](namespace: String, fn: ValueExtractor => Result[T]): Result[T]

    def optional[T](namespace: String, fn: ValueExtractor => Result[Option[T]]): Result[Option[T]]

    def nelist[T](namespace: String, fn: ValueExtractor => Result[List[T]]): Result[List[T]]

    def list[T](namespace: String, fn: ValueExtractor => Result[List[T]]): Result[List[T]]

}

object ValueExtractor {

    type Result[T] = StdEither[Error, T]

    sealed trait Error {
        def key: String
        def desc: String
    }

    object Error {

        case class Unexpected(
            override val key: String,
            override val desc: String,
            cause: Option[Throwable]
        ) extends Error

        case class InvalidKey(override val key: String, override val desc: String) extends Error

        case class UndefinedKey(override val key: String, override val desc: String) extends Error

        case class MissedValue(override val key: String, override val desc: String) extends Error

        case class EmptyValue(override val key: String, override val desc: String) extends Error

        case class InvalidValue[T](
            override val key: String, override val desc: String,
            convertor: Convertor[String, T], origin: String,
            cause: Option[Throwable]
        ) extends Error
    }

}
