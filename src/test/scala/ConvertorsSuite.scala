import org.scalatest.{FlatSpec, Matchers}

/**
  */
class ConvertorsSuite extends FlatSpec with Matchers{

    import com.bisphone.util.Convertors._

    it must "check (String, Int) parser" in {
        string2tupleofstringandint unsafe "0.0.0.0:1001" shouldEqual ("0.0.0.0", 1001)
        string2tupleofstringandint unsafe "localhost:1001" shouldEqual ("localhost", 1001)
        intercept[IllegalArgumentException] { string2tupleofstringandint unsafe "localhost1001:"}
        intercept[IllegalArgumentException] { string2tupleofstringandint unsafe ":localhost1001"}
        intercept[IllegalArgumentException] { string2tupleofstringandint unsafe "localhost1001"}
    }

    it must "check boolean parser" in {
        string2boolean unsafe "true" shouldEqual true
        string2boolean unsafe "on" shouldEqual true
        string2boolean unsafe "TrUe" shouldEqual true
        string2boolean unsafe "oN" shouldEqual true
        string2boolean unsafe "falSe" shouldEqual false
        string2boolean unsafe "oFf" shouldEqual false

        intercept[IllegalArgumentException] { string2boolean unsafe "tru" }
    }

}
