package com.bisphone.util

import com.bisphone.util.ReliabilityState.{ Green, Red, Shutdown, Yellow }


sealed trait ReliabilityState {
    def from: Long
    def toRed(time: Long = System.currentTimeMillis()): ReliabilityState = Red(time, this)
    def toYellow(time: Long = System.currentTimeMillis()): ReliabilityState  = Yellow(time, this)
    def toGreen(time: Long = System.currentTimeMillis()) : ReliabilityState = Green(time, this)
    def shtudown(time: Long = System.currentTimeMillis()): ReliabilityState = Shutdown(time, this)
}

object ReliabilityState {

    def apply(time: Long = System.currentTimeMillis()): ReliabilityState = Bootstrap(time)

    final case class Bootstrap private[ReliabilityState] (from: Long) extends ReliabilityState

    final case class Red[T <: ReliabilityState] private[ReliabilityState](
        from: Long, prev: T
    ) extends ReliabilityState


    final case class Yellow[T <: ReliabilityState] private[ReliabilityState](
        from: Long, prev: T
    ) extends ReliabilityState

    final case class Green[T <: ReliabilityState] private[ReliabilityState] (
        from: Long, prev: T
    ) extends ReliabilityState

    final case class Shutdown[T <: ReliabilityState] private[ReliabilityState](
        from: Long, prev: T
    ) extends ReliabilityState
}
