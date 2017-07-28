package com.bisphone.stdv1.util

import com.bisphone.stdv1.predef._
import ValueExtractor.Error
import ValueExtractor.Error._

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  *
  *
  *
  *             {{{
  *                 for {
  *                     config <- extactor.required[Path.Type]("config")
  *                     debug <- extactor.optional[Boolean]("debug")
  *                 } yield (config, debug)
  *             }}}
  */
class PosixArgumentExtractor(
    override val name: String,
    data: Iterable[String]
) extends ArgumentExtractor with PosixArgumentExtractor.Util with Module {

    override val logger = loadLogger

    logger.info(s"Init, data: ${data}")

    override def required[T](key: String)(
        implicit
        convertor: Convertor[String, T],
        executor: ExecutionContext
    ): AsyncResult[Error,T] = {
        fold(data.iterator, FindKey((mkKey(key)))) { (st, item) =>
            st match {
                case FindKey(impureKey) if item == impureKey =>
                    PeakValue(key)
                case PeakValue(impureKey) if isKey(item) =>
                    InternalError(MissedValue(key, s"Missed value for '${key}'"))
                case PeakValue(impureKey) =>
                    FinalValue(item)
                case _ => st
            }
        } match {
            case FinalValue(value) => convert[T](convertor, key, value)
            case PeakValue(_) => AsyncResult left MissedValue(key, s"Missed value for key: '${key}'")
            case FindKey(_) => AsyncResult left UndefinedKey(key, s"Undefined key: '${key}'")
            case InternalError(error) => AsyncResult left error
        }
    }

    override def optional[T](key: String)(
        implicit
        convertor: Convertor[String, T],
        executor: ExecutionContext
    ): AsyncResult[Error,Option[T]] = {
        required(key).map { _.some }.leftFlatMap {
            case UndefinedKey(_, _) => AsyncResult right none[T]
            case err => AsyncResult left err
        }
    }

    override def nelist[T](key: String)(
        implicit
        convertor: Convertor[String, T],
        executor: ExecutionContext
    ): AsyncResult[Error, List[T]] =
        fold(data.iterator, FindKey(mkKey(key))) { (st, item)  => st match {
            case FindKey(impureKey) if item == impureKey =>
                MultipleValue(key, Nil)
            case MultipleValue(_, Nil) if isKey(item) =>
                InternalError(MissedValue(key, s"Missed value for '${key}'"))
            case MultipleValue(key, Nil) =>
                MultipleValue(key, item :: Nil)
            case MultipleValue(_, list) if isKey(item) =>
                FinalValues(list)
            case MultipleValue(key, list) =>
                MultipleValue(key, item :: list)
            case _ => st
        }} match {
            case MultipleValue(_, Nil) => AsyncResult left MissedValue(key, s"Missed value for key: '${key}'")
            case MultipleValue(_, list) => convertList(convertor, key, list.reverse)
            case FinalValues(list) => convertList(convertor, key, list.reverse)
            case FindKey(_) => AsyncResult left UndefinedKey(key, s"Undefined key: '${key}'")
            case InternalError(error) => AsyncResult left error
        }

    override def list[T] (key: String)(
        implicit
        convertor: Convertor[String, T],
        executor: ExecutionContext
    ): AsyncResult[Error, List[T]] = {
        nelist(key).leftFlatMap{
            case UndefinedKey(_,_) => AsyncResult right Nil
            case err => AsyncResult left err
        }
    }

    override def firstOption[T] (
        implicit
        convertor: Convertor[String, T],
        executor: ExecutionContext
    ): AsyncResult[Error, Option[T]] = {
        data.headOption match {
            case None => AsyncResult right none
            case Some(v) if isKey(v) => AsyncResult right none
            case Some(v) => convert[T](convertor, "Not a key; Just first value", v) map { i:T => Some(i) }
        }
    }

}

object PosixArgumentExtractor {

    trait Util { self: PosixArgumentExtractor =>

        protected def mkKey(s: String) = "-" + s
        protected def isKey(s: String) = s startsWith "-"

        protected sealed trait State { def break: Boolean }
        protected sealed trait Complete extends State { override val break = true }
        protected sealed trait InComplete extends State  { override val break = false }
        protected final case class InternalError(error: ValueExtractor.Error) extends State  { override val break = true }

        protected final case class FindKey(val key: String) extends InComplete
        protected final case class PeakValue(val key: String) extends InComplete
        protected final case class MultipleValue(val key: String, val value: List[String]) extends InComplete

        protected final case class FinalValue(val value: String) extends Complete
        protected final case class FinalValues(val list: List[String]) extends Complete
        protected final case object NoValue extends Complete

        protected def fold[T](itr: Iterator[String], init: State)(f: (State, String) => State) = {
            var rsl = init
            while(itr.hasNext && !rsl.break) rsl = f(rsl, itr.next)
            rsl
        }

        protected def convert[T](
            convertor: Convertor[String, T],
            key: String,
            value: String
        )(
            implicit executor: ExecutionContext
        ): AsyncResult[Error, T] = {
            val rsl = catchNonFatal(convertor unsafe value) leftMap { thrown =>

                logger.debug(s"Convert failure for single-value, Key:${key}, Value:${value}, Convertor:${convertor.title}", thrown)

                InvalidValue(
                    key = key, desc = s"Convert failure for '${key}' = ${value} with ${convertor.title}",
                    convertor = convertor, origin = value, cause = Some(thrown)
                )
            }

            AsyncResult fromEither rsl
        }

        protected def convertList[T](
            convertor: Convertor[String, T],
            key: String,
            values: List[String]
        )(
            implicit executor: ExecutionContext
        ): AsyncResult[Error, List[T]] = {
            var lastValue: String = ""
            val rsl = catchNonFatal(values map {i =>
                lastValue = i
                convertor unsafe i
            }) leftMap { thrown =>

                logger.debug(s"Convert failure for list, Key:${key}, Value:${lastValue}, Convertor:${convertor.title}", thrown)

                InvalidValue(
                    key = key, desc = s"Convert failure for list '${key}' at specific vlaue: ${lastValue}) with ${convertor.title}",
                    convertor = convertor, origin = lastValue, cause = Some(thrown)
                )
            }
            AsyncResult fromEither rsl
        }

    }

}
