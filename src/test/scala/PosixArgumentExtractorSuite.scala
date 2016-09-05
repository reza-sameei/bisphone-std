
import org.scalatest._
import com.bisphone.util._
import com.bisphone.std._
import com.bisphone.util.PosixArgumentExtractor
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
class PosixArgumentExtractorSuite extends FlatSpec with Matchers with ScalaFutures {

  import ExecutionContext.Implicits.global

  val emptyList = new PosixArgumentExtractor(Nil)

  val myName = "reza"
  val myFamily = "samei"
  val myAge = 28

  val myProfile = new PosixArgumentExtractor(
    "-surname" :: myName ::
      "-familyname" :: myFamily ::
      "-age" :: myAge.toString ::
      // "family-pop" :: "6" :: // Optional - None
      "-interests" :: "family" :: "learning" ::
      "-nothing" :: // MissedValue
      "-somenumbers" :: "12" :: "0.4" :: "invalid" :: "34" ::
      "-projects" :: "RadsanWeb" :: "SMSPanel" :: "Bisphone" ::
      Nil
  )

  def unexp[T](t: T) =
    fail(s"Unexpected Response(${t.getClass.getName}): ${t}")

  "PosixArgumentExtractor" must "return UndefinedKey for required value" in {

    // In empty args-list
    whenReady((for {
      x <- emptyList.required[Int]("undefined")
    } yield x).asFuture) {
      case StdLeft(error) =>
        info(s"${error.getClass.getName} / ${error.desc}")
        error shouldBe a[UndefinedKey]
      case x => unexp(x)
    }


    // In non-empty args-list
    whenReady((for {
      x <- myProfile.required[Int]("undefined")
    } yield x).asFuture) {
      case StdLeft(error) =>
        info(s"${error.getClass.getName} / ${error.desc}")
        error shouldBe a[UndefinedKey]
      case x => unexp(x)
    }

  }

  it must "return MissedValue for required/optional value in non-empty args-list" in {

    whenReady((for {
      x <- myProfile.required[Int]("nothing")
    } yield x).asFuture) {
      case StdLeft(error) =>
        info(s"${error.getClass.getName} / ${error.desc}")
        error shouldBe a[MissedValue]
      case x => unexp(x)
    }

    whenReady((for {
      x <- myProfile.required[Int]("nothing")
    } yield x).asFuture) {
      case StdLeft(error) =>
        info(s"${error.getClass.getName} / ${error.desc}")
        error shouldBe a[MissedValue]
      case x => unexp(x)
    }
  }

  it must "return value (converted) for required/optional value and none for optinal inexists value" in {

    whenReady((for {
      surname <- myProfile.required[String]("surname")
      familyname <- myProfile.required[String]("familyname")
      age <- myProfile.optional[Int]("age")
      familyPopulation <- myProfile.optional[Int]("family-pop")
    } yield (surname, familyname, age, familyPopulation)).asFuture) {
      case StdRight((name, family, age, famliyPop)) =>
        assert(name ==  myName)
        assert(family == myFamily)
        assert(age.isDefined && age.get == myAge)
        assert(famliyPop.isEmpty)
      case x => unexp(x)
    }
  }

  it must "return a list of values" in {

    // MissedValue for list
    whenReady((for {
      x <- myProfile.list[String]("nothing")
    } yield x).asFuture){
      case StdLeft(error) =>
        error shouldBe a[MissedValue]
        info("'MissedValue' for missed-value keys")
      case x => unexp(x)
    }

    // From middle of the list
    whenReady((for {
      interests <- myProfile.list[String]("interests")
    } yield interests).asFuture) {
      case StdRight(list) =>
        list should have length 2
        list should equal("family" :: "learning" :: Nil)
        info("list from middle of arguments")
      case x => unexp(x)
    }

    // From end of arguments-list
    whenReady((for {
      projects <- myProfile.list[String]("projects")
    } yield projects).asFuture) {
      case StdRight(list: List[String] /*type erasure*/) =>
        list should have length 3
        list should equal("RadsanWeb" :: "SMSPanel" :: "Bisphone" :: Nil)
        info("list from end of arguments")
      case x => unexp(x)
    }
  }

  it must "return error for nelist (UndefinedKey/MisssedValue)" in {

    whenReady((for {
      xs <- myProfile.nelist[String]("undefined")
    }yield xs).asFuture) {
      case StdLeft(error) => error shouldBe a[UndefinedKey]
      case x => unexp(x)
    }

    whenReady((for {
      xs <- myProfile.nelist[String]("nothing")
    }yield xs).asFuture) {
      case StdLeft(error) => error shouldBe a[MissedValue]
      case x => unexp(x)
    }
  }

  it must "return convertion error" in {

    whenReady((for {
      invalid <- myProfile.required[Int]("surname")
    } yield invalid).asFuture) {
      case StdLeft(error) =>
        info(s"${error.getClass.getName} / ${error.desc}")
        error shouldBe a[InvalidValue[_]]
      case x => unexp(x)
    }

    whenReady((for {
      invalid <- myProfile.list[Int]("somenumbers")
    } yield invalid).asFuture) {
      case StdLeft(error) =>
        info(s"${error.getClass.getName} / ${error.desc}")
        error shouldBe a[InvalidValue[_]]
      case x => unexp(x)
    }

  }

}
