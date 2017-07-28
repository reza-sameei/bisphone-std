package com.bisphone.stdv1.util

import com.bisphone.stdv1.predef.Logger

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
trait Module {

    def name: String

    def loadLogger:Logger = Logger(name)

    def logger: Logger

}
