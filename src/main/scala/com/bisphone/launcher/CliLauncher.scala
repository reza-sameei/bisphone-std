package com.bisphone.launcher

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.concurrent.duration.Duration

import com.bisphone.std._
import com.bisphone.util.{ArgumentExtractor, AsyncResult, PosixArgumentExtractor}

class CliLauncher(name: String, tasks: Seq[Task]) {

    def main(args: Array[String]): Unit = CliLauncher(name)(tasks).runBy(args)

}

object CliLauncher {

    def apply(
        name: String,
        executionContext: ExecutionContextExecutor = ExecutionContext.global
    )(
        tasks : => Seq[Task]
    ) = new ContextBuilder(name, executionContext, tasks)

    class ContextBuilder(
        val name: String,
        val executionContext: ExecutionContextExecutor,
        val tasks: Seq[Task]
    ) {
        def build(extractor: ArgumentExtractor) = new Context(name, extractor, executionContext, tasks)

        def resultBy(extractor: ArgumentExtractor) = new Context(
            name, extractor, executionContext, tasks
        ).result

        def resultBy(args: Iterable[String]) = new Context(
            name,
            new PosixArgumentExtractor(args.toList),
            executionContext, tasks
        ).result

        def runBy(extractor: ArgumentExtractor) = new Context(
            name, extractor, executionContext, tasks
        ).run

        def runBy(args: Iterable[String]) = new Context(
            name,
            new PosixArgumentExtractor(args.toList),
            executionContext,
            tasks
        ).run
    }

    class Context(
        override val name: String,
        override val valueExtractor: ArgumentExtractor,
        implicit override val executionContext: ExecutionContextExecutor,
        val tasks: Seq[Task]
    ) extends LauncherContext with Module {

        override val logger = loadLogger

        logger.info("Init")

        private class DuplicatedTaskKey(
            val tasks: Seq[(Task, Task)]
        ) extends RuntimeException(
            s"Duplicated Task Keys: ${tasks.mkString(",")}"
        )

        private def indexTasks(
            tasks: Seq[Task]
        ): AsyncResult[Task.Result.Unsuccessful, Map[String, Task]] = AsyncResult lazyAsync {

            val buf = scala.collection.mutable.HashMap.empty[String, Task]

            val duplicated = scala.collection.mutable.ListBuffer.empty[(Task,Task)]

            val index = tasks.foldLeft(buf){ (buf, i) =>
                val props = i.props
                if (buf contains props.key) {
                    val first = buf(props.key)
                    duplicated append ((first, i))
                    logger.error(s"Init, Duplicated Task Key: ${props.key}, ${first}, ${i}")
                    buf
                } else {
                    logger.debug(s"Init, Add Task, ${props}, ${i}")
                    buf(props.key) = i
                    buf
                }
            }.toMap

            if (duplicated.nonEmpty) throw new DuplicatedTaskKey(duplicated)
            else index.stdright
        }

        private def pickTask (
            args: ArgumentExtractor, tasks: Map[String, Task]
        ): AsyncResult[Task.Result.Unsuccessful, Task] = AsyncResult lazyApply {

            args.firstOption[String] leftFlatMap { err =>
                Task.Result.usageError(s"Error in task name: ${err.desc}").asyncLeft
            } flatMap {
                case Some(name) if tasks contains name =>
                    tasks(name).asyncRight
                case Some(name) =>
                    Task.Result.usageError(s"Undefined Task: ${name}").asyncLeft
                case None =>
                    Task.Result.usageError("Not Specified Task!").asyncLeft
            }
        }


        private def extractTaskArgs (
            args: ArgumentExtractor,
            task: Task
        ): AsyncResult[Task.Result.Unsuccessful, Any] = AsyncResult lazyApply {
            (task.extract apply flat) leftFlatMap  { err =>
                logger.error(s"Error in extraction, ${err}")
                Task.Result.usageError(err.desc).asyncLeft
            }
        }

        private def runTask (
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

        private def exit (code: Int) = {
            sys.runtime.halt(code)
            ??? // Never
        }

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
    }
}
