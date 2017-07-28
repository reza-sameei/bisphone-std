package com.bisphone.stdv1.util

import com.bisphone.stdv1.predef._
import ValueExtractor._
import Error._

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
    val namespace: Option[String],
    data: Iterable[String]
) extends ArgumentExtractor with PosixArgumentExtractor.Util with Module {

    override val logger = loadLogger

    logger.info(s"Init, Namespace: ${namespace}, data: ${data}")

    override def required[T](key: String)(
        implicit convertor: Convertor[String, T]
    ): Result[T] = returnKey(key) flatMap { impureKey =>
        fold(data.iterator, FindKey((impureKey))) { (st, item) =>
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
            case PeakValue(_) => MissedValue(key, s"Missed value for key: '${key}'").stdleft
            case FindKey(_) => UndefinedKey(key, s"Undefined key: '${key}'").stdleft
            case InternalError(error) => error.stdleft
        }
    }

    override def required[T](namespace: String, fn: ValueExtractor => Result[T]): Result[T] =
        returnNS(namespace) flatMap { ns =>
            val inner = new PosixArgumentExtractor(s"${name}.inner", ns.some, data)
            fn(inner)
        }

    override def optional[T](key: String)(
        implicit convertor: Convertor[String, T]
    ): Result[Option[T]] = {
        required(key).map { _.some }.leftFlatMap {
            case UndefinedKey(_, _) =>  none[T].stdright
            case err => err.stdleft
        }
    }

    override def optional[T](namesapce: String, fn: ValueExtractor => Result[Option[T]]) =
        returnNS(namesapce) flatMap { ns =>
            val inner = new PosixArgumentExtractor(s"${name}.inner", ns.some, data)
            fn(inner)
        }

    override def nelist[T](key: String)(
        implicit convertor: Convertor[String, T]
    ): Result[List[T]] =
        returnKey(key) flatMap { impureKey =>
            fold(data.iterator, FindKey(impureKey)) { (st, item) =>
                st match {
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
                }
            } match {
                case MultipleValue(_, Nil) => MissedValue(key, s"Missed value for key: '${key}'").stdleft
                case MultipleValue(_, list) => convertList(convertor, key, list.reverse)
                case FinalValues(list) => convertList(convertor, key, list.reverse)
                case FindKey(_) => UndefinedKey(key, s"Undefined key: '${key}'").stdleft
                case InternalError(error) => error.stdleft
            }
        }

    override def nelist[T](namespace: String, fn: ValueExtractor => Result[List[T]]) =
        returnNS(namespace) flatMap { ns =>
            // @todo How to Iterate ?! for Posix?!
            Error.Unexpected(ns, s"Unsupported for '${getClass.getName}'", None).stdleft
        }

    override def list[T] (key: String)(
        implicit convertor: Convertor[String, T]
    ): Result[List[T]] = {
        nelist(key).leftFlatMap{
            case UndefinedKey(_,_) => Nil.stdright
            case err => err.stdleft
        }
    }

    override def list[T](namespace: String, fn: ValueExtractor => Result[List[T]]) = nelist(namespace, fn)

    override def firstOption[T] (
        implicit convertor: Convertor[String, T]
    ): Result[Option[T]] = {
        data.headOption match {
            case None => none.stdright
            case Some(v) if isKey(v) => none.stdright
            case Some(v) => convert[T](convertor, "Not a key; Just first value", v) map { i:T => Some(i) }
        }
    }

}

object PosixArgumentExtractor {

    trait Util { self: PosixArgumentExtractor =>

        val prefix = namespace match {
            case Some(ns) => s"-${ns.trim}."
            case None => "-"
        }

        protected def mkKey(s: String) = prefix + s
        protected def isKey(s: String) = s startsWith "-" // ?! s startsWith prefix

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

        protected def returnKey(key: String): ValueExtractor.Result[String] =
            key.trim match {
                case "" => Error.InvalidKey(key, s"Invalid Key: '${key}'").stdleft
                case key => mkKey(key).stdright
            }

        protected def returnNS(ns: String): ValueExtractor.Result[String] =
            ns.trim match {
                case "" => Error.InvalidKey(ns, s"Invalid Namespace: '${ns}'").stdleft
                case ns => ns.stdright
            }

        protected def convert[T](
            convertor: Convertor[String, T],
            key: String,
            value: String
        ): ValueExtractor.Result[T] = {
            catchNonFatal(convertor unsafe value) leftMap { thrown =>

                logger.debug(s"Convert failure for single-value, Key:${key}, Value:${value}, Convertor:${convertor.title}", thrown)

                InvalidValue(
                    key = key, desc = s"Convert failure for '${key}' = ${value} with ${convertor.title}",
                    convertor = convertor, origin = value, cause = Some(thrown)
                )
            }
        }

        protected def convertList[T](
            convertor: Convertor[String, T],
            key: String,
            values: List[String]
        ): ValueExtractor.Result[List[T]] = {
            var lastValue: String = ""
            catchNonFatal(values map {i =>
                lastValue = i
                convertor unsafe i
            }) leftMap { thrown =>

                logger.debug(s"Convert failure for list, Key:${key}, Value:${lastValue}, Convertor:${convertor.title}", thrown)

                InvalidValue(
                    key = key, desc = s"Convert failure for list '${key}' at specific vlaue: ${lastValue}) with ${convertor.title}",
                    convertor = convertor, origin = lastValue, cause = Some(thrown)
                )
            }
        }

    }

}
