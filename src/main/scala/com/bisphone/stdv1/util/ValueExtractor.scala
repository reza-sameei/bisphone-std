package com.bisphone.stdv1.util

import scala.concurrent.ExecutionContext

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */

trait ValueExtractor {

    def optional[T](key: String)(
        implicit
        convertor: Convertor[String, T],
        executor: ExecutionContext
    ): AsyncResult[ValueExtractor.Error,Option[T]]

    def required[T](key: String)(
        implicit
        convertor: Convertor[String, T],
        executor: ExecutionContext
    ): AsyncResult[ValueExtractor.Error, T]

    def list[T](key: String)(
        implicit
        convertor: Convertor[String, T],
        executor: ExecutionContext
    ): AsyncResult[ValueExtractor.Error, List[T]]

    def nelist[T](key: String)(
        implicit
        convertor: Convertor[String, T],
        executor: ExecutionContext
    ): AsyncResult[ValueExtractor.Error, List[T]]
}

object ValueExtractor {

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
