package com.bisphone.stdv1.util

import scala.concurrent.ExecutionContext

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
trait ArgumentExtractor extends ValueExtractor {
    def firstOption[T](
        implicit convertor: Convertor[String, T]
    ): ValueExtractor.Result[Option[T]]
}
