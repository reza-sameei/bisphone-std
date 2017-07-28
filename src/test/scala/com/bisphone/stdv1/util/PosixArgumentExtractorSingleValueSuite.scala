package com.bisphone.stdv1.util

import org.scalatest.{AsyncFlatSpec, Matchers}

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
class PosixArgumentExtractorSingleValueSuite extends AsyncFlatSpec with Matchers with TestUtil {

    import Convertors._
    import com.bisphone.stdv1.postdef.ValueExtractorSyntax._

    it must "return undefined-key for reqiured 'debug' in a multi-value data-list" in {
        val data = List("1", "2", "3")
        implicit val extractor = new PosixArgumentExtractor("1st", None, data)

        required[String]("debug").matchLeft {
            case err @ ValueExtractor.Error.UndefinedKey(key, desc) =>
                info(err.toString)
                succeed
        }
    }

    it must "return undefined-key for optional 'debug' in a multi-value data-list" in {
        val data = List("1", "2", "3")
        implicit val extractor = new PosixArgumentExtractor("1st", None, data)
        optional[String]("debug").matchRight {
            case None => succeed
        }
    }

    it must "return missed-value for required 'debug' in a multi-value data-list" in {
        val data = List("1", "2", "3", "-debug", "-another-key")
        implicit val extractor = new PosixArgumentExtractor("1st", None, data)
        required[String]("debug").matchLeft {
            case err @ ValueExtractor.Error.MissedValue(key, desc) =>
                info(err.toString)
                succeed
        }
    }

    it must "return missed-value for optional 'debug' in a multi-value data-list" in {
        val data = List("1", "2", "3", "-debug", "-another-key")
        implicit val extractor = new PosixArgumentExtractor("1st", None, data)
        optional[String]("debug").matchLeft {
            case err @ ValueExtractor.Error.MissedValue(key, desc) =>
                info(err.toString)
                succeed
        }
    }

    it must "return boolean value for 'debug' in a multi-value data-list" in {
        val data = List("1", "2", "3", "-debug", "true", "-another-key")
        implicit val extractor = new PosixArgumentExtractor("1st", None, data)
        required[String]("debug").matchRight {
            case "true" => succeed
        }
    }

    it must "return boolean value for 'debug' in a multi-value data-list + convert to boolean" in {
        val data = List("1", "2", "3", "-debug", "true", "-another-key")
        implicit val extractor = new PosixArgumentExtractor("1st", None, data)
        required[Boolean]("debug").matchRight {
            case true => succeed
        }
    }

    it must "return boolean value for required 'debug' in a multi-value data-list + convert to boolean - as last item in list" in {
        val data = List("1", "2", "3", "-debug", "true")
        implicit val extractor = new PosixArgumentExtractor("1st", None, data)
        required[Boolean]("debug").matchRight {
            case true => succeed
        }
    }

    it must "return boolean value for optional 'debug' in a multi-value data-list + convert to boolean - as last item in list" in {
        val data = List("1", "2", "3", "-debug", "true")
        implicit val extractor = new PosixArgumentExtractor("1st", None, data)

        optional[Boolean]("debug").matchRight {
            case Some(true) => succeed
        }
    }
}
