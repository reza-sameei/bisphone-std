package com.bisphone.stdv1.util

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  * @date 7/25/17
  */

object Path {
    type Type = java.nio.file.Path

    final case class Absolute(path: Type)
    final case class ReadableFile(path: Type)
}


