package com.bisphone.launcher

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.concurrent.duration.Duration

import com.bisphone.std._
import com.bisphone.util.{ArgumentExtractor, AsyncResult, PosixArgumentExtractor}
import com.typesafe.scalalogging.Logger

class CliLauncher(name: String, tasks: Seq[Task]) {

    def main(args: Array[String]): Unit = CliLauncher(name, ExecutionContext.global, tasks, args)

}

object CliLauncher {

    def exit (code: Int) = {
        sys.runtime.halt(code)
        ??? // Never
    }

    def apply(
        name: String,
        executionContext: ExecutionContextExecutor,
        tasks: Seq[Task],
        args: Seq[String]
    ) = ContextBuilder(name, tasks, executionContext) flatMap { builder =>
        val context = builder.build(new PosixArgumentExtractor(args.toList))
        context.result().sync(Duration.Inf).map{ i => (context, i) }
    } match {
        case StdSuccess((context, StdRight(Task.Result.Successful(msg)))) =>
            context.logger info s"Successful: ${msg}"
            exit(Task.Result.Done)

        case StdSuccess((context, StdLeft(Task.Result.Unsuccessful(code, message, cause)))) =>
            context.logger error (s"Error: ${message}", cause)
            exit(code)

        case StdFailure(cause: DuplicatedTaskKey) =>
            val buf = new StringBuilder("Failure: Duplicated Task Keys!")
            val msg = cause.tasks.foldLeft(buf) { (buf,i) =>
                val (first, second) = i
                buf append s"""
                                |  - ${first.props.key}
                                |    - ${first}
                                |    - ${second}
                         """.stripMargin
                buf
            }.toString
            println(msg)
            exit(Task.Result.Error)

        case StdFailure(cause) =>
            val msg = s"Failure: ${cause.getMessage}"
            println(msg)
            cause.printStackTrace()
            exit(Task.Result.Error)
    }

    class DuplicatedTaskKey(
        val tasks: Seq[(Task, Task)]
    ) extends RuntimeException (
        s"Duplicated Task Keys: ${tasks.mkString(",")}"
    )

    class ContextBuilder private[ContextBuilder](
        val name: String,
        val executionContext: ExecutionContextExecutor,
        val tasks: Map[String, Task]
    ) {

        def build(extractor: ArgumentExtractor) = new Context(
            name, extractor, executionContext, tasks
        )

        def build(args: Seq[String]) = new Context(
            name, new PosixArgumentExtractor(args), executionContext, tasks
        )
    }

    object ContextBuilder {

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
            tasks: => Seq[Task],
            ec: ExecutionContextExecutor
        ) = indexTasks(tasks) map { ts => new ContextBuilder(name, ec, ts) }
    }

    class Context private (
        override val name: String,
        tasks: Map[String, Task],
        override val valueExtractor: ArgumentExtractor,
        implicit override val executionContext: ExecutionContextExecutor,
    ) extends LauncherContext {

        override val logger = loadLogger

        logger.info("Init")

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

        def pickTask (
            args: ArgumentExtractor, tasks: Map[String, Task]
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

        def extractTaskArgs (
            args: ArgumentExtractor,
            task: Task
        ): AsyncResult[Task.Result.Unsuccessful, Any] = AsyncResult lazyApply {
            (task.extract apply flat) leftFlatMap  { err =>
                logger.error(s"Error in extraction, ${err}")
                Task.Result.usageError(err.desc).asyncLeft
            }
        }

        def runTask (
            ctx: LauncherContext.Flat,
            task: Task,
            args: Any
        ) =
            AsyncResult fromFuture {
                (task.run(ctx)(args.asInstanceOf[task.In])).map {
                    case done: Task.Result.Successful => done.stdright
                    case error: Task.Result.Unsuccessful => error.stdleft
                }
            }

        def result(): AsyncResult[Task.Result.Unsuccessful, Task.Result.Successful] = for {
            task <- pickTask(valueExtractor, tasks)
            args <- extractTaskArgs(valueExtractor, task)
            rsl <- runTask(flat, task, args)
        } yield rsl

    }

    /*class Context(
        override val name: String,
        override val valueExtractor: ArgumentExtractor,
        implicit override val executionContext: ExecutionContextExecutor,
        val tasks: Seq[Task]
    ) extends LauncherContext with Module {

        override val logger = loadLogger

        logger.info("Init")



        private def makeHelp(tasks: Map[String, Task]): String = {
            var builder = new StringBuilder("\n")

            builder = tasks.foldLeft(builder) { (buf, i) =>
                val task = i._2
                buf append s"${task.props.key}:\n\t${task.props.usage.replace("\n", "\n\t")}"
                buf
            }

            builder append "\n"

            builder.toString
        }



        def help = for {
            index <- indexTasks(tasks)
        } yield makeHelp(index)

        def result(): AsyncResult[Task.Result.Unsuccessful, Task.Result.Successful] = for {
            index <- indexTasks(tasks)
            task <- pickTask(valueExtractor, index)
            args <- extractTaskArgs(valueExtractor, task)
            rsl <- runTask(flat, task, args)
        } yield rsl

        def run(): Unit = result.sync(Duration.Inf) match {
            case StdSuccess(StdRight(Task.Result.Successful(msg))) =>
                logger.info(s"Successful: ${msg}")
                exit(Task.Result.Done)
            case StdSuccess(StdLeft(Task.Result.Unsuccessful(code, message, cause))) =>
                logger.error(s"Error: ${message}", cause)
                println(message)
                exit(code)
            case StdFailure(cause: DuplicatedTaskKey) =>
                val buf = new StringBuilder("Failure: Duplicated Task Keys!")
                val msg = cause.tasks.foldLeft(buf) { (buf,i) =>
                    val (first, second) = i
                    buf append s"""
                                    |  - ${first.props.key}
                                    |    - ${first}
                                    |    - ${second}
                         """.stripMargin
                    buf
                }.toString
                logger.error(msg)
                println(msg)
                exit(Task.Result.Error)
            case StdFailure(cause) =>
                val msg = s"Failure: ${cause.getMessage}"
                logger.error(msg, cause)
                println(msg)
                cause.printStackTrace()
                exit(Task.Result.Error)
        }
    }*/
}
