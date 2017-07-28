package com.bisphone.util

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
case class AsyncContext(
  private val executorInst: ExecutionContextExecutor,
  private val timeoutValue: Duration
) {
  implicit val executor = executorInst
  implicit val timeout = timeoutValue
}

object AsyncContext {

  val defaultInf = AsyncContext(ExecutionContext.global, Duration.Inf)

}
