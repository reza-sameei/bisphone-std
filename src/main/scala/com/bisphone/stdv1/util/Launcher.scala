/*
package com.bisphone.stdv1.util
import scala.concurrent.ExecutionContext
import com.bisphone.stdv1.predef._

/**
  */

class Luancher {
    def run(task: Task, context: LauncherContext) = ???
}

class UNNAMED_Extractor(
    first: ValueExtractor,
    second: ValueExtractor
) extends ValueExtractor {

    override def required[T] (key: String)(
        implicit convertor: Convertor[String, T]
    ): ValueExtractor.Result[T] = {
        first.required(key).leftFlatMap {
            case ValueExtractor.Error.UndefinedKey(_,_) => second.required(key)
            case err => err.stdleft
        }
    }

    override def optional[T] (key: String)(
        implicit convertor: Convertor[String, T]
    ): ValueExtractor.Result[Option[T]] = {

        /*required(key) map (_.some) leftFlatMap {
            case ValueExtractor.Error.UndefinedKey(_,_) => AsyncResult right none[T]
            case err => AsyncResult left err
        }*/

        required(key) match {
            case StdRight(value) => value.some.stdright
            case StdLeft(ValueExtractor.Error.UndefinedKey(_,_)) => none.stdright
            case StdLeft(err) => err.stdleft
        }
    }

    override def nelist[T] (key: String)(
        implicit convertor: Convertor[String, T]
    ): ValueExtractor.Result[List[T]] = {
        first.nelist(key) match {
            case StdRight(value) => value.stdright
            case StdLeft(ValueExtractor.Error.UndefinedKey(_,_)) => second.nelist(key)
            case StdLeft(err) => err.stdleft
        }
    }

    override def list[T] (key: String)(
        implicit
        convertor: Convertor[String, T]
    ): ValueExtractor.Result[List[T]]= {
        nelist(key) match {
            case StdRight(value) => value.stdright
            case StdLeft(ValueExtractor.Error.UndefinedKey(_,_)) => Nil.stdright
            case StdLeft(err) => err.stdleft
        }
    }
}*/
