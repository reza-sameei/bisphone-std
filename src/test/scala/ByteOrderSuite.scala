import com.bisphone.util.ByteOrder
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
class ByteOrderSuite extends FlatSpec with Matchers with ScalaFutures {


    "ByteOrder.LittleEndian" must "encode and decode bytes" in {

        import ByteOrder.LittleEndian._

        def byte(i: Byte) = decodeInt(encodeInt(i).iterator, 1).toByte

        val all = 1.toByte :: -1.toByte :: 0.toByte :: Byte.MaxValue :: Byte.MinValue :: Nil

        all map { i => byte(i) shouldEqual i }

    }

    it must "encode and decode shorts" in {

        import ByteOrder.LittleEndian._

        def short(i: Short) = decodeInt(encodeInt(i).iterator, 2).toShort

        val all =
            1000.toShort:: -5000.toShort :: Short.MaxValue :: Short.MinValue ::
                1.toShort :: -1.toShort :: 0.toShort :: Nil

        all map { i => short(i) shouldEqual i }

    }

    it must "encode and decode ints" in {

        import ByteOrder.LittleEndian._

        def int(i: Int) = decodeInt(encodeInt(i).iterator, 4)

        val all = 1 :: -1 :: 0 :: 1000 :: -5000 :: Int.MaxValue :: Int.MinValue :: Nil

        all map { i => int(i) shouldEqual i }

    }

    it must "encode and decode longs" in {

        import ByteOrder.LittleEndian._

        def long(i: Long) = decodeLong(encodeLong(i).iterator, 8)

        val all = 1l :: -1l :: 0l :: 1000l :: -5000l :: Long.MinValue :: Long.MaxValue :: Nil

        all map { i => long(i) shouldEqual i }

    }


}
