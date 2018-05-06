package com.bisphone.util


sealed trait ReliabilityState {

    import ReliabilityState._

    def since: Long
    def sameAs[T <: ReliabilityState](other: T): Boolean = {
        this.getClass == other.getClass
    }
    def isBootstrap: Boolean = this.isInstanceOf[Bootstrap]
    def isRed: Boolean = this.isInstanceOf[Red[_]]
    def isYello: Boolean = this.isInstanceOf[Yellow[_]]
    def isGreen: Boolean = this.isInstanceOf[Green[_]]
    def isShutdown: Boolean = this.isInstanceOf[Shutdown[_]]
}

object ReliabilityState {

    def apply(time: Long = System.currentTimeMillis()): ReliabilityState = Bootstrap(time)

    final case class Bootstrap (since: Long) extends ReliabilityState {
        def toRed(time: Long = System.currentTimeMillis()) = Red(time, this)
        def toYellow(time: Long = System.currentTimeMillis()) = Yellow(time, this)
        def toGreen(time: Long = System.currentTimeMillis()) = Green(time, this)
        def shutdown(time: Long = System.currentTimeMillis()) = Shutdown(time, this)
    }

    final case class Red[T <: ReliabilityState] (since: Long, prev: T) extends ReliabilityState {
        def toYellow(time: Long = System.currentTimeMillis()) = Yellow(time, this)
        def toGreen(time: Long = System.currentTimeMillis()) = Green(time, this)
        def shutdown(time: Long = System.currentTimeMillis()) = Shutdown(time, this)
    }

    object Red {
        def from[T <: ReliabilityState](time: Long, st: T): ReliabilityState = st match {
            case st:Red[_] => st
            case st => Red(time, st)
        }
    }


    final case class Yellow[T <: ReliabilityState] (since: Long, prev: T) extends ReliabilityState {
        def toRed(time: Long = System.currentTimeMillis()) = Red(time, this)
        def toGreen(time: Long = System.currentTimeMillis()) = Green(time, this)
        def shutdown(time: Long = System.currentTimeMillis()) = Shutdown(time, this)
    }

    object Yellow {
        def from[T <: ReliabilityState](time: Long, st: T): ReliabilityState = st match {
            case st:Yellow[_] => st
            case st => Yellow(time, st)
        }
    }

    final case class Green[T <: ReliabilityState] (since: Long, prev: T) extends ReliabilityState {
        def toRed(time: Long = System.currentTimeMillis()) = Red(time, this)
        def toYellow(time: Long = System.currentTimeMillis()) = Yellow(time, this)
        def shutdown(time: Long = System.currentTimeMillis()) = Shutdown(time, this)
    }

    object Green {
        def from[T <: ReliabilityState](time: Long, st: T): ReliabilityState = st match {
            case st:Green[_] => st
            case st => Green(time, st)
        }
    }

    final case class Shutdown[T <: ReliabilityState] (since: Long, prev: T) extends ReliabilityState
}
