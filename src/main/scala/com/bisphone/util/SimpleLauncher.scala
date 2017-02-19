package com.bisphone.util

import com.bisphone.std._

import scala.collection.mutable
import scala.concurrent.{Await, Promise}
import scala.util.control.NonFatal

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  * `
  * import com.bisphone.std._
  * import com.bisphone.std.util._
  *
  * object HelloWorld {
  *
  *   class Luancher(
  *     args: ArgumentExtractor,
  *     logger: SimpleLogger,
  *     context: AsyncContext
  *   ) extends SimpleLauncher(args, logger, context) {
  *
  *     override def usage = """
  *        | Usage
  *        |  say-hello
  *        |     -name            required
  *        |  version
  *        |""".stripMargin
  *
  *     task("say-hello", for{
  *       name <- args.required[String]("name")
  *     } yield name) { name =>
  *       logger println "Hello ${name}"
  *       success
  *     }
  *
  *     task("version") { logger println "1.0.0" ; success }
  *
  *   }
  *
  *   def main(args: Array[Byte]): Unit = {
  *     new Luancher(
  *       new PosixArgumentExtractor(args),
  *       SimpleLogger.default,
  *       AsyncContext.defaultInf
  *     ).run
  *   }
  *
  * }
  * `
  */

abstract class SimpleLauncher(
  val args: ArgumentExtractor,
  val logger: SimpleLogger,
  val asyncContext: AsyncContext
) {

  implicit private val ec = asyncContext.executor

  type TaskResult = AsyncResult[ExitStatus.UnsuccessStatus, Unit]

  type ConfigResult[T] = AsyncResult[SimpleError, T]

  sealed private trait Task {
    def run: AsyncResult[ExitStatus.UnsuccessStatus, Unit]
  }

  final private class SimpleTask(
    f: => TaskResult
  ) extends Task {
    override def run: TaskResult =
      try f catch {
        case NonFatal(cause) => AsyncResult fromFailure cause
      }
  }

  final private class FeaturedTask[T](
    config: => ConfigResult[T],
    task: T => TaskResult
  ) extends Task {
    override def run: TaskResult = {
      val p = Promise[StdEither[ExitStatus.UnsuccessStatus,Unit]]

      config.asFuture.onComplete {
        case StdSuccess(StdRight(value)) =>
          p completeWith task(value).asFuture
        case StdSuccess(StdLeft(error)) =>
          logger println s"Configuration error: ${error.desc}"
          if (error.cause.isDefined) error.cause.get printStackTrace logger.asWriter
          p success ExitStatus.ConfigurationError.stdleft
        case StdFailure(cause) => p failure cause
      }

      AsyncResult fromFuture p.future
    }

  }

  private val tasks = mutable.HashMap.empty[String, Task]

  final protected def task(command: String)(fn: => TaskResult): Unit =
    tasks(command) = new SimpleTask(fn)

  final protected def task[T](command: String, config: => ConfigResult[T])(
    fn: T => TaskResult
  ): Unit = tasks(command) = new FeaturedTask[T](config, fn)

  def run(): Unit = {

    val firstStep: AsyncResult[ExitStatus.UnsuccessStatus, Option[String]] =
      args.firstOption[String].leftMap { error: SimpleError =>
        logger println s"Unexpected error in reading 'command': ${error.desc}"
        ExitStatus.GeneralError
      }

    val rsl = firstStep.flatMap {
      case None => default()
      case Some(command) if tasks contains command => tasks(command).run
      case Some(undefined) =>
        logger println s"Undefined Command (${undefined.getClass.getName}): ${undefined}"
        logger println usage
        ExitStatus.UsageError.asyncLeft
    }

    rsl.asFuture onComplete {
      case StdSuccess(StdRight(unit: Unit)) =>
        logger println s"Exit: 0"
        sys.runtime.halt(0)
      case StdSuccess(StdLeft(exitStatus)) =>
        logger println s"Exit: ${exitStatus}"
        sys.runtime.halt(exitStatus.code)
      case StdFailure(cause) =>
        logger println s"Error(${cause.getClass.getName}): ${cause.getMessage}"
        // cause printStackTrace logger.asWriter
        cause.printStackTrace()
        sys.runtime.halt(ExitStatus.GeneralError.code)
    }

    try Await.ready(rsl.asFuture, asyncContext.timeout) catch {
      case cause: Throwable =>
        logger println s"End with error: ${cause.getClass.getName}: ${cause.getMessage}"
        cause printStackTrace logger.asWriter
    }
  }

  // utilities

  final def success = ().asyncRight

  final def errorGeneral = ExitStatus.GeneralError.asyncLeft

  // to override

  def default(): TaskResult = {
    logger println usage
    success
  }

  def usage: String

}
