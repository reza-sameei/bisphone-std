package com.bisphone.launcher

import scala.concurrent.ExecutionContextExecutor

import com.typesafe.scalalogging.Logger

trait RuntimeContext {

    def name: String

    def logger: Logger

    def executionContext: ExecutionContextExecutor
}
