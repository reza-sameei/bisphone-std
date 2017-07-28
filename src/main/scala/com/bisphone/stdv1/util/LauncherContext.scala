package com.bisphone.stdv1.util

import com.bisphone.stdv1._
import com.bisphone.stdv1.util.LauncherContext.FlatAdapter

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */

trait LauncherContext { self =>

    def logger: Logger

    def extractor: ValueExtractor

    def executionContext: ExecutionContext

    final def flat: LauncherContext.Flat = new FlatAdapter(self)

}

object LauncherContext {

    /**
      * In implicit-convertion scala compiler just know 'how' to convert A => B
      * but it's not so smart that can determine 'what' can be converted!
      * or 'what' he/she should convert!
      *
      * It's usefull just when he/she know we have 'A' but need 'B' (for example in argument list of a function)
      * then it uses 'A => B'
      * but when we just know we want 'B'; compiler can't find 'A' ?! because he/she don't know
      * 'Should I use A? or other type? Have I any convertor avaiable for that(we can focus on this question and find solution)?'
      *
      * for this readon I made a 'Flat' version of LauncherContext that let it be used as a simple implicit param!
      */
    trait Flat extends LauncherContext with ValueExtractor with ExecutionContext {}

    class FlatAdapter(ctx: LauncherContext) extends Flat {

        override def execute (runnable: Runnable): Unit = ctx.executionContext.execute(runnable)

        override def reportFailure (cause: Throwable): Unit = ctx.executionContext.reportFailure(cause)

        override def optional[T] (key: String)(implicit convertor: Convertor[String, T], executor: ExecutionContext): AsyncResult[ValueExtractor.Error, Option[T]] = ctx.extractor.optional(key)

        override def required[T] (key: String)(implicit convertor: Convertor[String, T], executor: ExecutionContext): AsyncResult[ValueExtractor.Error, T] = ctx.extractor.required(key)

        override def list[T] (key: String)(implicit convertor: Convertor[String, T], executor: ExecutionContext): AsyncResult[ValueExtractor.Error, List[T]] = ctx.extractor.list(key)

        override def nelist[T] (key: String)(implicit convertor: Convertor[String, T], executor: ExecutionContext): AsyncResult[ValueExtractor.Error, List[T]] = ctx.extractor.nelist(key)

        override def logger: _root_.com.bisphone.stdv1.Logger = ctx.logger

        override def extractor: ValueExtractor = ctx.extractor

        override def executionContext = ctx.executionContext
    }
}