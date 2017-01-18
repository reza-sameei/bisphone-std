package com.bisphone.util

import java.nio.ByteBuffer

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */

object LongCodec {

  trait Encoder { def encodeLong(value: Long): Array[Byte] }

  trait Decoder { def decodeLong(bytes: Iterator[Byte], len: Int): Long }

  trait BigEndianEncoder extends Encoder {

    def encodeLong(value: Long): Array[Byte] = {
      Array[Byte](
        (value >> 56).toByte,
        (value >> 48).toByte,
        (value >> 40).toByte,
        (value >> 32).toByte,
        (value >> 24).toByte,
        (value >> 16).toByte,
        (value >> 8).toByte,
        value.toByte
      )
    }
  }

  trait BigEndianDecoder extends Decoder {

    def decodeLong(bytes: Iterator[Byte], len: Int): Long = {
      var count = len
      var decoded: Long = 0L
      while (count > 0) {
        decoded <<= 8
        decoded |= bytes.next().toInt & 0xFF
        count -= 1
      }
      decoded
    }

  }

  trait LittleEndianEncoder extends Encoder {

    def encodeLong(value: Long): Array[Byte] = {
      Array[Byte](
        value.toByte,
        (value >> 8).toByte,
        (value >> 16).toByte,
        (value >> 24).toByte,
        (value >> 32).toByte,
        (value >> 40).toByte,
        (value >> 48).toByte,
        (value >> 56).toByte
      )
    }

  }

  trait LittleEndianDecoder extends Decoder {

    def decodeLong(bytes: Iterator[Byte], len: Int): Long = {

      val highestOctet = (len.toLong - 1) * 8
      val Mask = if (len % 8 == 0) -1 else 1L << (len * 8)
      var count = len
      var decode: Long = 0l

      while (count > 0) {
        decode >>>= 8
        decode += (bytes.next.toLong & 0xFF) << highestOctet
        count -= 1
      }

      decode & Mask
    }

  }

}
