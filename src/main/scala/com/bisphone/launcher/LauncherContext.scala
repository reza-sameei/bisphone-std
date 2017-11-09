package com.bisphone.launcher

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration.Duration

import com.bisphone.util._
import com.typesafe.scalalogging.Logger
import com.bisphone.std._


trait LauncherContext extends RuntimeContext with Module { self =>

    override def logger: Logger

    override def executionContext: ExecutionContextExecutor

    def valueExtractor: ValueExtractor

    final protected def flat() = new LauncherContext.Flat(self)

}

object LauncherContext {

    class Flat(ctx: LauncherContext)
        extends LauncherContext
            with ValueExtractor
            with ExecutionContextExecutor {

        override def execute (runnable: Runnable): Unit = ctx.executionContext.execute(runnable)

        override def reportFailure (cause: Throwable): Unit = ctx.executionContext.reportFailure(cause)

        override def required[T] (key: String)(
            implicit
            convertor: Convertor[String, T],
            executionContextExt: ExecutionContextExecutor = ctx.executionContext
        ): ValueExtractor.Result[T] = ctx.valueExtractor.required(key)

        // override def required[T](namespace: String, fn: ValueExtractor => ValueExtractor.Result[T]): ValueExtractor.Result[T] = ctx.extractor.required(namespace, fn)

        override def optional[T] (key: String)(
            implicit
            convertor: Convertor[String, T],
            executionContextExecutor: ExecutionContextExecutor = ctx.executionContext
        ): ValueExtractor.Result[Option[T]] = ctx.valueExtractor.optional(key)

        // override def optional[T](namespace: String, fn: ValueExtractor => ValueExtractor.Result[T]): ValueExtractor.Result[Option[T]] = ctx.valueExtractor.optional(namespace, fn)

        override def nelist[T] (key: String)(
            implicit
            convertor: Convertor[String, T],
            executionContextExecutor: ExecutionContextExecutor
        ): ValueExtractor.Result[List[T]] = ctx.valueExtractor.nelist(key)

        // override def nelist[T](namespace: String, fn: ValueExtractor => ValueExtractor.Result[T]): ValueExtractor.Result[List[T]] = ctx.valueExtractor.nelist(namespace, fn)

        override def list[T] (key: String)(
            implicit
            convertor: Convertor[String, T],
            executionContextExecutor: ExecutionContextExecutor
        ): ValueExtractor.Result[List[T]] = ctx.valueExtractor.list(key)

        // override def list[T](namespace: String, fn: ValueExtractor => ValueExtractor.Result[T]): ValueExtractor.Result[List[T]] = ctx.valueExtractor.list(namespace, fn)

        override def valueExtractor: ValueExtractor = ctx.valueExtractor

        override def executionContext = ctx.executionContext

        override def logger = ctx.logger

        override def name = ctx.name

        override protected def loadLogger = ctx.logger
    }

}