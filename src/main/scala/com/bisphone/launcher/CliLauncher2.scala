package com.bisphone.launcher

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.concurrent.duration.Duration

import com.bisphone.util.{ArgumentExtractor, AsyncResult, PosixArgumentExtractor, ValueExtractor}
import com.bisphone.std._
import com.typesafe.scalalogging.Logger

class CliLauncher2(
    override val name: String,
    tasks: Map[String, Task],
    ec: ExecutionContextExecutor
) extends Module {

    override val logger = loadLogger

    val help: String = {

        var builder = new StringBuilder("\n")

        builder = tasks.foldLeft(builder) { (buf, i) =>
            val task = i._2
            buf append s"${task.props.key}:\n\t${task.props.usage.replace("\n", "\n\t")}"
            buf
        }

        builder append "\n"

        builder.toString
    }

    def context(
        args: ArgumentExtractor
    ): AsyncResult[Task.Result.Unsuccessful, CliLauncher2.Context] =
        CliLauncher2.Context(
            s"${name}.launcher-context",
            ec,
            args
        ).asyncRight

    def extractTask (
        args: ArgumentExtractor
    ): AsyncResult[Task.Result.Unsuccessful, Task] = AsyncResult lazyApply {

        args.firstOption[String] leftFlatMap { err =>
            Task.Result.usageError(s"Error in task name: ${err.desc}").asyncLeft
        } flatMap {
            case Some(name) if tasks contains name =>
                tasks(name).asyncRight
            case Some(name) =>
                Task.Result.usageError(s"Undefined Task: ${name}\n${help}").asyncLeft
            case None =>
                Task.Result.usageError("Not Specified Task!").asyncLeft
        }
    }

    def extractTaskArguments(
        ctx: CliLauncher2.Context,
        task: Task
    ): AsyncResult[Task.Result.Unsuccessful, Any] = AsyncResult lazyApply {
        (task.extract apply ctx.flat) leftFlatMap  { err =>
            logger.error(s"Error in extraction, ${err}")
            Task.Result.usageError(err.desc).asyncLeft
        }
    }

    def runTask (
        ctx: CliLauncher2.Context,
        task: Task,
        args: Any
    ) =
        AsyncResult fromFuture {
            (task.run(ctx.flat)(args.asInstanceOf[task.In])).map {
                case done: Task.Result.Successful => done.stdright
                case error: Task.Result.Unsuccessful => error.stdleft
            }
        }

    def runBy(
        args: ArgumentExtractor
    ): AsyncResult[Task.Result.Unsuccessful, Task.Result.Successful] = for {
        ctx <- context(args)
        task <- extractTask(args)
        args <- extractTaskArguments(ctx, task)
        rsl <- runTask(ctx, task, args)
    } yield rsl

}

object CliLauncher2 {

    class DuplicatedTaskKey(
        val tasks: Seq[(Task, Task)]
    ) extends RuntimeException (
        s"Duplicated Task Keys: ${tasks.mkString(",")}"
    )

    private def indexTasks(
        tasks: Seq[Task]
    ): StdTry[Map[String, Task]] = Try {

        val buf = scala.collection.mutable.HashMap.empty[String, Task]

        val duplicated = scala.collection.mutable.ListBuffer.empty[(Task,Task)]

        val index = tasks.foldLeft(buf){ (buf, i) =>
            val props = i.props
            if (buf contains props.key) {
                val first = buf(props.key)
                duplicated append ((first, i))
                buf
            } else {
                buf(props.key) = i
                buf
            }
        }.toMap

        if (duplicated.nonEmpty) throw new DuplicatedTaskKey(duplicated)
        else index
    }

    def apply(
        name: String,
        tasks: => Map[String, Task],
    ec: ExecutionContextExecutor
    ): CliLauncher2 = new CliLauncher2(name, tasks, ec)

    def apply(
        name: String,
        tasks: => Seq[Task],
        ec: ExecutionContextExecutor
    ): StdTry[CliLauncher2] = indexTasks(tasks) map { ts => CliLauncher2(name, ts, ec) }

    case class Context(
        override val name: String,
        override val executionContext: ExecutionContextExecutor,
        override val valueExtractor: ValueExtractor
    ) extends LauncherContext {

        override def logger: Logger = loadLogger
    }

    def exit (code: Int) = {
        sys.runtime.halt(code)
        ??? // Never
    }

    class Main(name: String, tasks: Seq[Task]) {

        def main (args: Array[String]): Unit = try {

            val extractor = new PosixArgumentExtractor(args)

            val ec = ExecutionContext.global

            CliLauncher2(name, tasks, ec) match {
                case StdSuccess(launcher) =>

                    val result = launcher
                        .runBy(extractor)
                        .sync(Duration.Inf)

                    result match {
                        case StdSuccess(StdRight(Task.Result.Successful(msg))) =>
                            launcher.logger info s"Successful: ${msg}"
                            exit(Task.Result.Done)

                        case StdSuccess(StdLeft(Task.Result.Unsuccessful(code, message, cause))) =>
                            launcher.logger error (s"Error: ${message}", cause)
                            exit(code)

                        case StdFailure(cause: DuplicatedTaskKey) =>
                            val buf = new StringBuilder("Failure: Duplicated Task Keys!")
                            val msg = cause.tasks.foldLeft(buf) { (buf,i) =>
                                val (first, second) = i
                                buf append s"""
                                                |  - ${first.props.key}
                                                |    - ${first}
                                                |    - ${second}
                                                |    """.stripMargin
                                buf
                            }.toString
                            launcher.logger.error(msg)
                            exit(Task.Result.Error)

                        case StdFailure(cause) =>
                            val msg = s"Failure: ${cause.getMessage}"
                            launcher.logger.error(msg, cause)
                            exit(Task.Result.Error)
                    }

                case StdFailure(cause) =>
                    println(s"Unhandled Failure")
                    cause.printStackTrace()
                    exit(Task.Result.Error)
            }
        }

    }
}
