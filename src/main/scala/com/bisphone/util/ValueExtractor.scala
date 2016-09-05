package com.bisphone.util

import com.bisphone.std._

import scala.concurrent.ExecutionContextExecutor

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */

sealed trait ExtractError extends SimpleError

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
  ): AsyncResult[ExtractError,Option[T]] = ???

  def required[T](key: String)(
    implicit cnvt: Convertor[String, T],
    executor: ExecutionContextExecutor
  ): AsyncResult[ExtractError, T] =
    optional[T](key).flatMap { i: Option[T] => i match {
      case None => UndefinedKey(key, s"Undefined key '${key}'}").asyncLeft
      case Some(value) => value.asyncRight[ExtractError]
    }}

  def list[T](key: String)(
    implicit cntv: Convertor[String, T],
    executor: ExecutionContextExecutor
  ): AsyncResult[ExtractError, List[T]] = ???

  def nelist[T](key: String)(
    implicit cntv: Convertor[String, T],
    executor: ExecutionContextExecutor
  ): AsyncResult[ExtractError, List[T]] =
    list[T](key) flatMap { v: List[T] => v match {
      case Nil => UndefinedKey(key, s"Empty list for '${key}'").asyncLeft
      case list => list.asyncRight
    }}

}

trait ArgumentExtractor extends ValueExtractor {
  def firstOption[T](
    implicit cnvt: Convertor[String, T],
    executor: ExecutionContextExecutor
  ): AsyncResult[ExtractError, Option[T]]
}

