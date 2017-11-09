
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

import com.bisphone.launcher.{CliLauncher, Task}
import com.bisphone.launcher.Task.{Props, Result}
import com.bisphone.launcher.Task.Result.Unsuccessful
import org.scalatest.{FlatSpec, _}
import com.bisphone.util._
import com.bisphone.std._
import com.bisphone.util.PosixArgumentExtractor
import org.scalatest.concurrent.ScalaFutures

class CliLauncherSuite extends FlatSpec with Matchers with ScalaFutures{

    class HelloX(name: String) extends Task {

        override type In = (String, Boolean)

        override def props = Task.Props(name, "-name STRING [-debug true]")

        override def extract = { implicit ctx => for {
            name <- required[String]("name")
            debug <- optional[Boolean]("debug")
        } yield (name, debug getOrElse false) }

        override def run = func { implicit ctx => {
            case (name, debug) =>
                println(s"Hi ${name}!")
                successful("Done")
        }}
    }


    it must "return correct values base on input arguments" in {



        val tasks = Map(
            "say-hello" -> new HelloX("say-hello")
        )

        val launcher = CliLauncher(
            "test-1",
            tasks,
            ExecutionContext.global
        )

        info(launcher.help)

        launcher.runBy(new PosixArgumentExtractor(Nil)).sync(Duration.Inf) match {
            case StdSuccess(StdLeft(Result.Unsuccessful(Result.Usage, msg, None))) => info(msg)
            case unexp => fail(s"Unexpected: ${unexp}")
        }

        launcher.runBy(new PosixArgumentExtractor(
            "say-hello" :: Nil
        )).sync(Duration.Inf) match {
            case StdSuccess(StdLeft(Result.Unsuccessful(Result.Usage, msg, None))) => info(msg)
            case unexp => fail(s"Unexpected: ${unexp}")
        }

        launcher.runBy(new PosixArgumentExtractor(
            "say-hello" :: "-name" :: Nil
        )).sync(Duration.Inf) match {
            case StdSuccess(StdLeft(Result.Unsuccessful(Result.Usage, msg, None))) => info(msg)
            case unexp => fail(s"Unexpected: ${unexp}")
        }

        launcher.runBy(new PosixArgumentExtractor(
            "say-hello" :: "-name" :: "Reza" :: Nil
        )).sync(Duration.Inf) match {
            case StdSuccess(StdRight(Result.Successful(msg))) => info(msg)
            case unexp => fail(s"Unexpected: ${unexp}")
        }

        launcher.runBy(new PosixArgumentExtractor( // Try 'name'!
            "say-hello" :: "-name" :: "Reza" ::  "debug" :: "true" :: Nil
        )).sync(Duration.Inf) match {
            case StdSuccess(StdRight(Result.Successful(msg))) => info(msg)
            case unexp => fail(s"Unexpected: ${unexp}")
        }

        launcher.runBy(new PosixArgumentExtractor( // Try 'name'!
            "say-hello" :: "-name" :: "Reza" ::  "-debug" :: Nil
        )).sync(Duration.Inf) match {
            case StdSuccess(StdLeft(Result.Unsuccessful(Result.Usage, msg, None))) => info(msg)
            case unexp => fail(s"Unexpected: ${unexp}")
        }

        launcher.runBy(new PosixArgumentExtractor( // Try 'name'!
            "say-hello" :: "-name" :: "Reza" ::  "-debug" :: "error" :: Nil
        )).sync(Duration.Inf) match {
            case StdSuccess(StdLeft(Result.Unsuccessful(Result.Usage, msg, None))) => info(msg)
            case unexp => fail(s"Unexpected: ${unexp}")
        }

        launcher.runBy(new PosixArgumentExtractor( // Try 'name'!
            "say-hello" :: "-name" :: "Reza" ::  "-debug" :: "false" :: Nil
        )).sync(Duration.Inf) match {
            case StdSuccess(StdRight(Result.Successful(msg))) => info(msg)
            case unexp => fail(s"Unexpected: ${unexp}")
        }
    }

}
