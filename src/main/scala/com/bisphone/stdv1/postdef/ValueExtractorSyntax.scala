package com.bisphone.stdv1.postdef

import com.bisphone.stdv1.util.{ArgumentExtractor, AsyncResult, Convertor, ValueExtractor}

import scala.concurrent.ExecutionContext

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
trait ValueExtractorSyntax {

    def required[T](key: String)(
        implicit
        convertor: Convertor[String, T],
        executor: ExecutionContext,
        extractor: ValueExtractor
    ): AsyncResult[ValueExtractor.Error, T] = extractor required key

    def optional[T](key: String)(
        implicit
        convertor: Convertor[String, T],
        executor: ExecutionContext,
        extractor: ValueExtractor
    ): AsyncResult[ValueExtractor.Error, Option[T]] = extractor optional key

    def nelist[T](key: String)(
        implicit
        convertor: Convertor[String, T],
        executor: ExecutionContext,
        extractor: ValueExtractor
    ): AsyncResult[ValueExtractor.Error, List[T]] = extractor nelist key

    def list[T](key: String)(
        implicit
        convertor: Convertor[String, T],
        executor: ExecutionContext,
        extractor: ValueExtractor
    ): AsyncResult[ValueExtractor.Error, List[T]] = extractor list key

    def firstOption[T](
        implicit
        convertor: Convertor[String, T],
        executor: ExecutionContext,
        extractor: ArgumentExtractor
    ): AsyncResult[ValueExtractor.Error, Option[T]] = extractor firstOption

}

object ValueExtractorSyntax extends ValueExtractorSyntax