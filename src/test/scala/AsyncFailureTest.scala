import org.scalatest.{AsyncFlatSpec, MustMatchers}
import scala.concurrent.ExecutionContext.Implicits.global
import com.bisphone.std._
import com.bisphone.util._

class AsyncFailureTest extends AsyncFlatSpec with MustMatchers {

    it must "?" in {

        val fn: (Int => AsyncResult[String, Int]) = { x:Int =>
            info("Throw Exception ...")
            throw new Exception("HA HA HA")
        }

        val rsl = AsyncResult.right[String, Int](12) flatMap { v =>
            info("Enter FlatMap")
            fn(v)
        }

        recoverToSucceededIf[Exception](rsl.asFuture)
    }

}
