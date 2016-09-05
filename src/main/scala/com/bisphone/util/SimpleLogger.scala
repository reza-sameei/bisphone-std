package com.bisphone.util

import java.io.PrintWriter

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
trait SimpleLogger {
  def println(str: String)
  def asWriter: PrintWriter
}

object SimpleLogger {


  val default: SimpleLogger = new SimpleLogger {
    private val out = Console.out
    override val asWriter = new PrintWriter(Console.out)
    override def println (str: String): Unit = out println str

  }
}
