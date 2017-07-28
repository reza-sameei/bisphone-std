package com.bisphone.stdv1.predef

import scala.util.control.NonFatal

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
trait Funcs {

    @inline final def catchNonFatal[T](f: => T): StdEither[Throwable,T] = try {
        StdRight(f)
    } catch {
        case NonFatal(cause:Throwable) => StdLeft(cause)
    }

}

object Funcs extends Funcs
