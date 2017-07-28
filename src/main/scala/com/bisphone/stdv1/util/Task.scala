package com.bisphone.stdv1.util

import com.bisphone.stdv1.StdFuture
import com.bisphone.stdv1.postdef.ValueExtractorSyntax
import sun.nio.ch.sctp.SctpStdSocketOption

/**
  */

sealed trait TaskResult {
    def message: String
    def code: Int
    override def toString = s"${getClass.getName}(${message}, ${code})"
}

object TaskResult {

    final case class Successful (override val message: String) extends TaskResult {
        override val code: Int = 0
    }

    sealed trait Unsuccessful extends TaskResult {
        val cause: Option[Throwable]
    }

    /*
        There is no standard about status-codes;
        https://stackoverflow.com/questions/1101957/are-there-any-standard-exit-status-codes-in-linux
        I've decided to use a custom code pattern:
            0 -> success
            1 -> general error
            0x10 -> usage & config error
            0x20 -> environment error
            0x30 -> remote error
            0x40 -> internal error
     */

    final case class Error(override val message: String, override val code: Int = 1, override val cause: Option[Throwable] = None) extends Unsuccessful


    /**
      * Usage and/or Configuration Error!
      * @param message
      */
    final case class UsageAndConfigurationErorr(override val message: String, override val cause: Option[Throwable] = None) extends Unsuccessful {
        override val code : Int = 0x10
    }

    /**
      * Environment Error!
      * - permission denined on reading/writing/executing files
      * - unavaialble resources such file, network, ...
      * @param message
      */
    final case class EnvironmentError (override val message: String, override val cause: Option[Throwable] = None) extends Unsuccessful {
        override val code: Int = 0x20
    }

    /**
      * Remote Error
      * - Unavaible service
      * - Unexptected response
      * - etc
      * @param message
      */
    final case class RemoteError (override val message: String, override val cause: Option[Throwable] = None) extends Unsuccessful {
        override val code: Int = 0x30
    }

    /**
      * Uncategorized & Unexpected Failures!
      * @param message
      */
    final case class InternalError(override val message: String, override val cause: Option[Throwable] = None) extends Unsuccessful {
        override val code: Int = 0x40
    }
}

case class TaskProps(key: String, usage: String)



trait TaskSyntax extends ValueExtractorSyntax { self: Task =>

    protected def successful(message: String) = TaskResult.Successful(message)

    protected def generalError(message: String, code: Int): TaskResult.Unsuccessful = TaskResult.Error(message, code, None)
    protected def generalError(message: String, code: Int, cause: Throwable): TaskResult.Unsuccessful = TaskResult.Error(message, code, Some(cause))

    protected def usageError(message: String): TaskResult.Unsuccessful = TaskResult.UsageAndConfigurationErorr(message, None)
    protected def usageError(message: String, cause: Throwable): TaskResult.Unsuccessful = TaskResult.UsageAndConfigurationErorr(message, Some(cause))

    protected def configError(message: String): TaskResult.Unsuccessful = TaskResult.UsageAndConfigurationErorr(message, None)
    protected def configError(message: String, cause: Throwable): TaskResult.Unsuccessful = TaskResult.UsageAndConfigurationErorr(message, Some(cause))

    protected def environmentError(message: String): TaskResult.Unsuccessful = TaskResult.EnvironmentError(message, None)
    protected def environmentError(message: String, cause: Throwable): TaskResult.Unsuccessful = TaskResult.EnvironmentError(message, Some(cause))

    protected def remoteError(message: String): TaskResult.Unsuccessful = TaskResult.RemoteError(message, None)
    protected def remoteError(message: String, cause: Throwable): TaskResult.Unsuccessful = TaskResult.RemoteError(message, Some(cause))

    protected def internalError(message: String): TaskResult.Unsuccessful = TaskResult.InternalError(message, None)
    protected def internalError(message: String, cause: Throwable): TaskResult.Unsuccessful = TaskResult.InternalError(message, Some(cause))

    protected implicit def syntax$taskresult$to$future(rsl: TaskResult): StdFuture[TaskResult] = StdFuture successful rsl

    protected def func(fn: => Fn): Fn = fn

    @inline protected def debug(message: String)(implicit context: LauncherContext.Flat): Unit = context.logger.debug(message)

    @inline protected def debug(message: String, exception: Throwable)(implicit context: LauncherContext.Flat): Unit = context.logger.debug(message, exception)

    @inline protected def trace(message: String)(implicit context: LauncherContext.Flat): Unit = context.logger.trace(message)

    @inline protected def trace(message: String, exception: Throwable)(implicit context: LauncherContext.Flat): Unit = context.logger.trace(message, exception)

    @inline protected def info(message: String)(implicit context: LauncherContext.Flat): Unit = context.logger.info(message)

    @inline protected def info(message: String, exception: Throwable)(implicit context: LauncherContext.Flat): Unit = context.logger.info(message, exception)

    @inline protected def warn(message: String)(implicit context: LauncherContext.Flat): Unit = context.logger.warn(message)

    @inline protected def warn(message: String, exception: Throwable)(implicit context: LauncherContext.Flat): Unit = context.logger.warn(message, exception)

    @inline protected def error(message: String)(implicit context: LauncherContext.Flat): Unit = context.logger.error(message)

    @inline protected def erro(message: String, exception: Throwable)(implicit context: LauncherContext.Flat): Unit = context.logger.error(message, exception)
}

trait Task extends TaskSyntax {
    type In
    type Fn = LauncherContext.Flat => In => StdFuture[TaskResult]
    def props: TaskProps
    def extract: LauncherContext.Flat => ValueExtractor.Result[In]
    def run: Fn
}
