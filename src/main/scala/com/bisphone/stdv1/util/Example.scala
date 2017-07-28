package com.bisphone.stdv1.util

import com.bisphone.stdv1.util.Path.ReadableFile

import scala.concurrent.ExecutionContext

/**
  */
class Example {

    object HttpUp extends Task {

        import com.bisphone.stdv1.util.Convertors._

        override def props: TaskProps = TaskProps("httpup", "Usage: location -config {config-path}")

        override type In = (ReadableFile, Boolean, Int)

        override def extract = { implicit ctx: LauncherContext.Flat => for {
            config <- required[ReadableFile]("config")
        } yield (config, true, 1212) }

        override def run  = func {
            implicit ctx => {
                case (readableFile, verbose, port) =>
                    debug("Here")
                    error("Hello")
                    successful("done")
            }
        }
    }

    CliLauncher("main", ExecutionContext.Implicits.global)(
        HttpUp :: Nil
    ).runBy("" :: "" :: "" :: Nil)
}
