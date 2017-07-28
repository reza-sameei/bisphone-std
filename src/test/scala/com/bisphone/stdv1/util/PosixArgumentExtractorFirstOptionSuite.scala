package com.bisphone.stdv1.util

import org.scalatest.{Assertion, FlatSpec, Matchers, AsyncFlatSpec}
import com.bisphone.stdv1._

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
class PosixArgumentExtractorFirstOptionSuite extends AsyncResultFlastSpec with Matchers {

    import Convertors._
    import com.bisphone.stdv1.postdef.ValueExtractorSyntax._

    it must "return error for firstOption in an empty data-list" in {
        val data = Nil
        implicit val extractor = new PosixArgumentExtractor("1st", data)

        firstOption[String].matchRight {
            case None => succeed
        }
    }

    it must "return first value for firstOption in a single-value data-list" in {
        val data = List("hello")
        implicit val extractor = new PosixArgumentExtractor("2nd", data)
        firstOption[String].matchRight {
            case Some("hello") => succeed
        }
    }

    it must "return first value for firstOption in a multi-value data-list" in {
        val data = List("1", "2", "3")
        implicit val extractor = new PosixArgumentExtractor("3rd", data)
        firstOption[String].matchRight {
            case Some("1") => succeed
        }
    }

}
