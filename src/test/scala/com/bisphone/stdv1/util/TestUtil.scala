package com.bisphone.stdv1.util

import com.bisphone.stdv1._
import org.scalatest.{Assertion, Assertions}

/**
  */
trait TestUtil  { self: Assertions =>

    implicit class EitherTestOperations[L,R](val self: StdEither[L,R]) extends {

        def onRight(fn : R => Assertion): Assertion = {
            self match {
                case StdRight(value) => fn(value)
                case StdLeft(value) => fail(s"Left Value: ${value}")
            }
        }

        def matchRight(fn: PartialFunction[R, Assertion]): Assertion = {
            self.onRight { value =>
                if (fn.isDefinedAt(value)) fn(value)
                else fail(s"Unmatched Right Value: ${value}")
            }
        }

        def onLeft(fn: L => Assertion): Assertion = {
            self match {
                case StdRight(value) => fail(s"Right Value: ${value}")
                case StdLeft(value) => fn(value)
            }
        }

        def matchLeft(fn: PartialFunction[L, Assertion]): Assertion = {
            self.onLeft { value =>
                if (fn.isDefinedAt(value)) fn(value)
                else fail(s"Unmatched Left Value: ${value}")
            }
        }
    }

    implicit class AsyncResultTestOperations[L,R](val self: AsyncResult[L,R]) {

        def onRight(fn : R => Assertion)(
            implicit ec: ExecutionContext
        ): StdFuture[Assertion] = self.asFuture.map { _.onRight(fn) }

    def matchRight(fn: PartialFunction[R, Assertion])(
        implicit ec: ExecutionContext
    ): StdFuture[Assertion] = {
        self.onRight { value =>
            if (fn.isDefinedAt(value)) fn(value)
            else fail(s"Unmatched Right Value: ${value}")
        }
    }

    def onLeft(fn: L => Assertion)(
        implicit ec: ExecutionContext
    ): StdFuture[Assertion] = {
        self.asFuture.map { _.onLeft(fn) }
    }

    def matchLeft(fn: PartialFunction[L, Assertion])(
        implicit ec: ExecutionContext
    ): StdFuture[Assertion] = {
        self.onLeft { value =>
            if (fn.isDefinedAt(value)) fn(value)
            else fail(s"Unmatched Left Value: ${value}")
        }
    }
}

}
