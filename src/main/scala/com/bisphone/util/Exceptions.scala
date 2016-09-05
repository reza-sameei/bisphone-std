package com.bisphone.util

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
trait CheapException extends RuntimeException {
  final override def fillInStackTrace = this
}

sealed class RootException(
  message: String,
  cause: Throwable = null
) extends RuntimeException(message, cause)

class LogicException(
  message: String,
  cause: Throwable = null
) extends RootException(message, cause)

class ResourceException(
  message: String,
  cause: Throwable = null
) extends RootException(message, cause)

class RemoteException(
  message: String,
  cause: Throwable = null
) extends RootException(message, cause)