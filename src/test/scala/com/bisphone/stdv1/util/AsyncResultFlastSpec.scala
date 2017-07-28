package com.bisphone.stdv1.util

import com.bisphone.stdv1._
import org.scalatest.{Assertion, AsyncFlatSpec}

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  * @date 7/25/17
  */
trait AsyncResultFlastSpec extends AsyncFlatSpec {

    implicit class AsyncResultTestOperations[L,R](val self: AsyncResult[L,R]) {

        def onRight(fn : R => Assertion): StdFuture[Assertion] = {
            self.asFuture.map {
                case StdRight(value) => fn(value)
                case StdLeft(value) => fail(s"Left Value: ${value}")
            }
        }

        def matchRight(fn: PartialFunction[R, Assertion]): StdFuture[Assertion] = {
            self.onRight { value =>
                if (fn.isDefinedAt(value)) fn(value)
                else fail(s"Unmatched Right Value: ${value}")
            }
        }

        def onLeft(fn: L => Assertion): StdFuture[Assertion] = {
            self.asFuture.map {
                case StdRight(value) => fail(s"Right Value: ${value}")
                case StdLeft(value) => fn(value)
            }
        }

        def matchLeft(fn: PartialFunction[L, Assertion]): StdFuture[Assertion] = {
            self.onLeft { value =>
                if (fn.isDefinedAt(value)) fn(value)
                else fail(s"Unmatched Left Value: ${value}")
            }
        }
    }

}
