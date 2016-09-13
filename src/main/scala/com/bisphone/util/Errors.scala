package com.bisphone.util

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */

sealed trait Error

trait SimpleError extends Error {
  def desc: String
  def cause: Option[Throwable]
}

case class Errors(list: List[Error]) extends Error

// SimpleError

case class GeneralError(
  override val desc: String,

  override val cause: Option[Throwable] = None
) extends SimpleError
