package com.bisphone.util

import com.bisphone.std._
import scala.concurrent.ExecutionContextExecutor

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
class PosixArgumentExtractor(
  data: Iterable[String]
) extends ArgumentExtractor {

  private def mkKey(s: String) = "-" + s
  private def isKey(s: String) = s startsWith "-"

  private sealed trait State { def break: Boolean }
  private sealed trait Complete extends State { override val break = true }
  private sealed trait InComplete extends State  { override val break = false }
  private final case class Error(error: ExtractError) extends State  { override val break = true }

  // @todo: fix the warning "The outer reference in this type test cannot be checked at run time."
  // Scala 2.12.3 will show this warning!
  // @readAbout: https://stackoverflow.com/questions/16450008/typesafe-swing-events-the-outer-reference-in-this-type-test-cannot-be-checked-a

  private final case class FindKey(val key: String) extends InComplete
  private final case class PeakValue(val key: String) extends InComplete
  private final case class MultipleValue(val key: String, val value: List[String]) extends InComplete

  private final case class FinalValue(val value: String) extends Complete
  private final case class FinalValues(val list: List[String]) extends Complete
  private final case object NoValue extends Complete

  private def fold(itr: Iterator[String], init: State)(f: (State, String) => State) = {
    var rsl = init
    while(itr.hasNext && !rsl.break) rsl = f(rsl, itr.next)
    rsl
  }

  private def convert[T](
    convertor: Convertor[String, T],
    key: String,
    value: String
  )(
    implicit executor: ExecutionContextExecutor
  ): AsyncResult[ExtractError, T] = {
    val rsl = catchNonFatal(convertor unsafe value) leftMap { thrown =>
      InvalidValue(
        key, value, convertor,
        s"Can't convert value('${key}' = ${value}) with ${convertor.title}",
        Some(thrown)
      )
    }
    AsyncResult fromEither rsl
  }

  private def convertList[T](
    convertor: Convertor[String, T],
    key: String,
    values: List[String]
  )(
    implicit executor: ExecutionContextExecutor
  ): AsyncResult[ExtractError, List[T]] = {
    var lastValue: String = ""
    val rsl = catchNonFatal(values map {i =>
      lastValue = i
      convertor unsafe i
    }) leftMap { thrown =>
      InvalidValue(
        key, lastValue, convertor,
        s"Can't convert value('${key}' = ${lastValue}) with ${convertor.title}",
        Some(thrown)
      )
    }
    AsyncResult fromEither rsl
  }

  override def optional[T](key: String)(
    implicit cnvt: Convertor[String, T],
    executor: ExecutionContextExecutor
  ): AsyncResult[ExtractError,Option[T]] = {

    val rsl = fold(data.iterator, FindKey((mkKey(key)))) { (st, item) => st match {
      case FindKey(impureKey) if item == impureKey =>
        PeakValue(key)
      case PeakValue(impureKey) if isKey(item) =>
        Error(MissedValue(key, s"Missed value for '${key}'"))
      case PeakValue(impureKey) =>
        FinalValue(item)
      case _ => st
    }}

    (rsl: @unchecked) match {
      case FinalValue(value) => convert[T](cnvt, key, value) map { i: T => Some(i) }
      case PeakValue(_) => AsyncResult left MissedValue(key, s"The '${key}' needs a value", None)
      case FindKey(_) => None.asyncRight
      case Error(error) => error.asyncLeft
    }
  }

  override def list[T](key: String)(
    implicit cnvt: Convertor[String, T],
    executor: ExecutionContextExecutor
  ): AsyncResult[ExtractError, List[T]] = {
    val rsl = fold(data.iterator, FindKey(mkKey(key))) { (st, item) =>
      st match {
        case FindKey(impureKey) if item == impureKey =>
          MultipleValue(key, Nil)
        case MultipleValue(_, Nil) if isKey(item) =>
          Error(MissedValue(key, s"Missed value for '${key}'"))
        case MultipleValue(key, Nil) =>
          MultipleValue(key, item :: Nil)
        case MultipleValue(_, list) if isKey(item) =>
          FinalValues(list)
        case MultipleValue(key, list) =>
          MultipleValue(key, item :: list)
        case _ => st
      }
    }
    (rsl: @unchecked) match {
      case MultipleValue(_, Nil) => MissedValue(key, s"MissedValue for '${key}'", None).asyncLeft
      case MultipleValue(_, list) => convertList(cnvt, key, list.reverse)
      case FinalValues(list) => convertList(cnvt, key, list.reverse)
      case FindKey(_) => Nil.asyncRight
      case Error(error) => error.asyncLeft
    }
  }

  def firstOption[T](
    implicit cnvt: Convertor[String, T],
    executor: ExecutionContextExecutor
  ): AsyncResult[ExtractError, Option[T]] = {
    data.headOption match {
      case None => None.asyncRight
      case Some(v) if isKey(v) => None.asyncRight
      case Some(v) => convert[T](cnvt, "Not a key; Just first value", v) map { i:T => Some(i) }
    }
  }

}
