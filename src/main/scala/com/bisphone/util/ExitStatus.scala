package com.bisphone.util

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */

sealed class ExitStatus private[ExitStatus](val code: Int, val desc: String)

object ExitStatus {

  sealed class UnsuccessStatus private[ExitStatus](code: Int, desc: String) extends ExitStatus(code, desc)

  final case object Success extends ExitStatus(0, "Success")

  final case object GeneralError extends UnsuccessStatus(1, "General Error")

  final case object UsageError extends UnsuccessStatus(2, "Usage Error")

  final case object ConfigurationError extends UnsuccessStatus(78, "Configuration Error")

  // For case-classes we should use `override` because compiler automatically don't make them as arguments-only !
  final case class Error(override val code: Int, override val desc: String) extends UnsuccessStatus(code, desc)

}
