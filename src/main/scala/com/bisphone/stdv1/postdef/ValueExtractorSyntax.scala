package com.bisphone.stdv1.postdef

import com.bisphone.stdv1.util.{ArgumentExtractor, AsyncResult, Convertor, ValueExtractor}

import scala.concurrent.ExecutionContext

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
trait ValueExtractorSyntax {

    import ValueExtractor._

    def required[T](key: String)(
        implicit
        convertor: Convertor[String, T],
        extractor: ValueExtractor
    ): Result[T] = extractor required key

    def optional[T](key: String)(
        implicit
        convertor: Convertor[String, T],
        extractor: ValueExtractor
    ): Result[Option[T]] = extractor optional key

    def nelist[T](key: String)(
        implicit
        convertor: Convertor[String, T],
        extractor: ValueExtractor
    ): Result[List[T]] = extractor nelist key

    def list[T](key: String)(
        implicit
        convertor: Convertor[String, T],
        extractor: ValueExtractor
    ): Result[List[T]] = extractor list key

    def firstOption[T](
        implicit
        convertor: Convertor[String, T],
        extractor: ArgumentExtractor
    ): Result[Option[T]] = extractor firstOption

}

object ValueExtractorSyntax extends ValueExtractorSyntax