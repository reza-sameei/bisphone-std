package com.bisphone.stdv1.util

import com.bisphone.stdv1.{Duration, ExecutionContext}
import com.bisphone.stdv1.predef._
import Convertors._

/**
  */
object CliLauncher {

    def apply(
        name: String, executionContext: ExecutionContext
    )(
        tasks : => Seq[Task]
    ) = new ContextBuilder(name, executionContext, tasks)

    class ContextBuilder(
        val name: String,
        val executionContext: ExecutionContext,
        val tasks: Seq[Task]
    ) {
        def build(extractor: ArgumentExtractor) = new Context(name, extractor, executionContext, tasks)

        def runBy(extractor: ArgumentExtractor) = new Context(name, extractor, executionContext, tasks).run

        def runBy(args: Iterable[String]) = new Context(
            name,
            new PosixArgumentExtractor(s"${name}.posix-argument-extractor", None, args.toList),
            executionContext,
            tasks
        ).run
    }

    class Context(
        override val name: String,
        override val extractor: ArgumentExtractor,
        implicit override val executionContext: ExecutionContext,
        val tasks: Seq[Task]
    ) extends LauncherContext with Module {

        override def logger = loadLogger

        val index = tasks.map(i => i.props.key -> i).toMap

        private[CliLauncher] def pickTask (args: ArgumentExtractor, tasks: Seq[Task]): AsyncResult[TaskResult.Unsuccessful, Task] = try {
            val rsl = args.firstOption[String] match {
                case StdRight(Some(name)) if index contains name => index(name).stdright
                case StdRight(Some(name)) => TaskResult.Error(s"Undefined Task: ${name}").stdleft
                case StdRight(None) => TaskResult.Error("Not Specified Task!").stdleft
            }
            AsyncResult fromEither rsl
        } catch {
            case cause: Throwable => AsyncResult fromFailure cause
        }


        private[CliLauncher] def extractTaskArgs (
            args: ArgumentExtractor, task: Task
        ): AsyncResult[TaskResult.Unsuccessful, Any] =
            AsyncResult fromEither (task.extract apply flat).leftMap { err =>
                logger.error(s"Error in extraction: ${err}")
                TaskResult.Error(err.desc)
            }

        private[CliLauncher] def runTask (ctx: LauncherContext.Flat, task: Task, args: Any) =
            AsyncResult fromFuture {
                (task.run(ctx)(args.asInstanceOf[task.In])).map {
                    case done: TaskResult.Successful => done.stdright
                    case err: TaskResult.Unsuccessful => err.stdleft
                }
            }

        private[CliLauncher] def exit (code: Int) = {
            sys.runtime.halt(code)
            ??? // Never
        }

        def run(): Unit = {

            val rsl = for {
                task <- pickTask(extractor, tasks)
                args <- extractTaskArgs(extractor, task)
                rsl <- runTask(flat, task, args)
            } yield rsl

            rsl.get(Duration.Inf) match {
                case StdSuccess(StdRight(successful)) =>
                    logger.info(successful.message)
                    exit(successful.code)
                case StdSuccess(StdLeft(unsuccessful)) =>
                    logger.error(unsuccessful.message, unsuccessful.cause.orNull)
                    exit(unsuccessful.code)
                case StdFailure(cause) =>
                    logger.error("Failure", cause)
                    exit(1)
            }
        }
    }

}