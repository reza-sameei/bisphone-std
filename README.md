in the name of ALLAH

### A collection of utilities, aliases, etc. in Scala;


##### com.bisphone.util.Launcher

A tool to develop simple and easy argument/options from cli; compined from 
`com.bisphone.util.ValueExtractor` and `com.bisphone.util.AsyncResult` and other things.

```scala
package com.bisphone.example

import com.bisphone.std._
import com.bisphone.util._

object Main {

  def main(args: Array[String]): Unit = new Inner(args).run

  class Inner(
    list: Array[String]
  ) extends SimpleLauncher(
    new PosixArgumentExtractor(list),
    SimpleLogger.default,
    AsyncContext.defaultInf
  ) {

    import Convertors._
    import asyncContext._

    override def usage =
      s"""
         | sayhello                           print the NAME
         |      -name    NAME
       """.stripMargin

    task("sayhello", for {
      str <- args.required[String]("name")
    } yield str ) { str =>
        logger println s"Hello ${str}"
        success
    }
    
    task("version") {
        logger println s"0.1.7-SNAPSHOT"
        success
    }
    
    task("start",for {
        filePath <- args.required[String]("conf")
    } yield new java.io.File(filePath)) { configFile =>
        // ...
        success
    }
  }

}
```

#### com.bisphone.util.AsyncResult
A monad transformer over `Future` and `Either`

```scala
package com.bisphone.std._
package com.bisphone.util._
import scala.concurrent._

class AsyncResultExample {

  import ExecutionContext.Implitics.global

  // AsyncResult is a transformer for Future[StdEither[L,R]]

  val a: AsyncRsult[String, Int] = (Future successful 1.stdright).asAsyncResult

  val b: AsyncResult[Int,Int] = a leftMap { str => str.toInt } // also leftFlatMap

  val c: AsyncResult[Int,Int] = b rcover {
    case cause: SomeExpectedException => -1.asyncLeft // AsyncResult left -1
  }

  val d: AsyncResult[String, Int] = (Future successful 2.stdleft).asAsyncResult

  val e: AsyncResult[String, Int] = for {
    first <- a
    seconf <- b
  } yield a << b

  val f: Future[StdEither[String, Int]] = e.asFuture


}
```

##### com.bisphone.util.Convertor
An abstraction for convertor behavior between types; Use as type-class/evidence in some other tools such as `ValueExtractor`
```scala

import com.bisphone.util.Convertors._
import scala.concurrent.duration._

def finiteDurationFromString(str: String)(
    implicit convertor: Convertor[String, FiniteDuration]
): FiniteDuration = convertor.unsafe(str)


```

##### com.bisphone.util.ValueExtractor

@Todo :)

##### com.bisphone.util.ByteOrder

An ADT with Int & Long encoder/decoder for BigEndian & LittleEndian.

##### com.bisphone.util.CheapException

A trait to make exceptions cheaper :)

#### And ...

Check source code for more :)