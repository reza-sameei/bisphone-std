package com.bisphone.util


sealed trait ReliabilityState { def from: Long }

object ReliabilityState {

    final case class Bootstramp (from: Long) extends ReliabilityState {
        def toRed(time: Long = System.currentTimeMillis()): ReliabilityState = Red(time, this)
        def toYellow(time: Long = System.currentTimeMillis()): ReliabilityState  = Yellow(time, this)
        def toGreen(time: Long = System.currentTimeMillis()) : ReliabilityState = Green(time, this)
        def shtudown(time: Long = System.currentTimeMillis()): ReliabilityState = Shutdown(time, this)
    }

    final case class Red[T <: ReliabilityState] private[ReliabilityState](
        from: Long, prev: T
    ) extends ReliabilityState {
        def toYellow(time: Long = System.currentTimeMillis()): ReliabilityState  = Yellow(time, this)
        def toGreen(time: Long = System.currentTimeMillis()) : ReliabilityState = Green(time, this)
        def shtudown(time: Long = System.currentTimeMillis()): ReliabilityState = Shutdown(time, this)
    }


    final case class Yellow[T <: ReliabilityState] private[ReliabilityState](
        from: Long, prev: T
    ) extends ReliabilityState {
        def toRed(time: Long = System.currentTimeMillis()): ReliabilityState = Red(time, this)
        def toGreen(time: Long = System.currentTimeMillis()) : ReliabilityState = Green(time, this)
        def shtudown(time: Long = System.currentTimeMillis()): ReliabilityState = Shutdown(time, this)
    }

    final case class Green[T <: ReliabilityState] private[ReliabilityState] (
        from: Long, prev: T
    ) extends ReliabilityState {
        def toRed(time: Long = System.currentTimeMillis()): ReliabilityState = Red(time, this)
        def toYellow(time: Long = System.currentTimeMillis()): ReliabilityState  = Yellow(time, this)
        def shtudown(time: Long = System.currentTimeMillis()): ReliabilityState = Shutdown(time, this)
    }

    final case class Shutdown[T <: ReliabilityState] private[ReliabilityState](
        from: Long, prev: T
    ) extends ReliabilityState
}
