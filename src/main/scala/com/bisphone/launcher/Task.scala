package com.bisphone.launcher

import scala.concurrent.Future

import com.bisphone.util.{ArgumentExtractor, ValueExtractor}

object Task {

    sealed trait Result

    object Result {

        case class Successful(message: String) extends Result
        case class Unsuccessful(code: Int, message: String ,cause: Option[Throwable]) extends Result

        val Done = 0x0
        val Error = 0x1

        val Usage = 0x10
        val Config = 0x20
        val Enviroment = 0x30
        val Remote = 0x40
        val Internal = 0x50

        def successful(message: String) = Result.Successful(message)

        def usageError(message: String) = Unsuccessful(Usage, message, None)
        def usageError(message: String, cause: Throwable) = Unsuccessful(Usage, message, Some(cause))

        def configError(message: String) = Unsuccessful(Config, message, None)
        def configError(message: String, cause: Throwable)= Unsuccessful(Config, message, Some(cause))

        def environmentError(message: String) = Unsuccessful(Enviroment, message, None)
        def environmentError(message: String, cause: Throwable) = Unsuccessful(Enviroment, message, Some(cause))

        def remoteError(message: String) = Unsuccessful(Remote, message, None)
        def remoteError(message: String, cause: Throwable) = Unsuccessful(Remote, message, Some(cause))

        def internalError(message: String) = Unsuccessful(Internal, message, None)
        def internalError(message: String, cause: Throwable) = Unsuccessful(Internal, message, Some(cause))
    }

    case class Props(key: String, usage: String)

    trait Syntax extends ArgumentExtractor.Syntax { self =>

        import Result._

        @inline protected def successful(message: String): Result = Result.Successful(message)

        @inline protected def usageError(message: String): Result = Unsuccessful(Usage, message, None)
        @inline protected def usageError(message: String, cause: Throwable): Result = Unsuccessful(Usage, message, Some(cause))

        @inline protected def configError(message: String): Result = Unsuccessful(Config, message, None)
        @inline protected def configError(message: String, cause: Throwable): Result = Unsuccessful(Config, message, Some(cause))

        @inline protected def environmentError(message: String): Result = Unsuccessful(Enviroment, message, None)
        @inline protected def environmentError(message: String, cause: Throwable): Result = Unsuccessful(Enviroment, message, Some(cause))

        @inline protected def remoteError(message: String): Result = Unsuccessful(Remote, message, None)
        @inline protected def remoteError(message: String, cause: Throwable): Result = Unsuccessful(Remote, message, Some(cause))

        @inline protected def internalError(message: String): Result = Unsuccessful(Internal, message, None)
        @inline protected def internalError(message: String, cause: Throwable): Result = Unsuccessful(Internal, message, Some(cause))

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

}

trait Task extends Task.Syntax {

    type In
    type Fn = LauncherContext.Flat => In => Future[Task.Result]

    def props: Task.Props
    def extract: LauncherContext.Flat => ValueExtractor.Result[In]
    def run: Fn

    protected implicit def syntax$taskresult$to$future(rsl: Task.Result): Future[Task.Result] = Future successful rsl
    protected def func(fn: => Fn): Fn = fn
    override def toString(): String = {
        val ref = super.toString.replace(getClass.getName, "")
        s"${getClass.getName}(${props},${ref})"
    }
}