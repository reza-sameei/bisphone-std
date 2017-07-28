package com.bisphone.stdv1.util

import scala.concurrent.ExecutionContext

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
trait ArgumentExtractor extends ValueExtractor {
    def firstOption[T](
        implicit cnvt: Convertor[String, T],
        executor: ExecutionContext
    ): AsyncResult[ValueExtractor.Error, Option[T]]
}
