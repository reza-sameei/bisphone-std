package com.bisphone.util

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */

object IntCodec {

  trait Encoder { def encodeInt(value: Int): Array[Byte] }

  trait Decoder { def decodeInt(bytes: Iterator[Byte], len: Int): Int }

  trait BigEndianEncoder extends Encoder {

    def encodeInt(value: Int): Array[Byte] = {
      Array[Byte](
        (value >> 24).toByte,
        (value >> 16).toByte,
        (value >> 8).toByte,
        value.toByte
      )
    }
  }

  trait BigEndianDecoder extends Decoder {

    def decodeInt(bytes: Iterator[Byte], len: Int): Int = {
      var count = len
      var decoded: Int = 0
      while (count > 0) {
        decoded <<= 8
        decoded |= bytes.next().toInt & 0xFF
        count -= 1
      }
      decoded
    }

  }

  trait LittleEndianEncoder extends Encoder {

    def encodeInt(value: Int): Array[Byte] = {
      Array[Byte](
        value.toByte,
        (value >> 8).toByte,
        (value >> 16).toByte,
        (value >> 24).toByte
      )
    }

  }

  trait LittleEndianDecoder extends Decoder {

    def decodeInt(bytes: Iterator[Byte], len: Int): Int = {
      val highestOctet = (len - 1) << 3 // << 3 = * 8
      val Mask = ((1L << (len << 3)) - 1).toInt
      var count = len
      var decoded = 0
      while (count > 0) {
        decoded >>>= 8
        decoded += (bytes.next().toInt & 0xFF) << highestOctet
        count -= 1
      }
      decoded & Mask
    }

  }

}