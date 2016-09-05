package com.bisphone.util

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
sealed abstract class ByteOrder private[ByteOrder](
  val name: String,
  val javaValue: java.nio.ByteOrder
) extends IntCodec.Encoder with IntCodec.Decoder with LongCodec.Encoder with LongCodec.Decoder

object ByteOrder {

  object BigEndian extends ByteOrder("big-endian", java.nio.ByteOrder.BIG_ENDIAN)
    with IntCodec.BigEndianEncoder with IntCodec.BigEndianDecoder
    with  LongCodec.BigEndianEncoder with LongCodec.BigEndianDecoder

  object LittleEndian extends ByteOrder("little-endian", java.nio.ByteOrder.LITTLE_ENDIAN)
    with IntCodec.LittleEndianEncoder with IntCodec.LittleEndianDecoder
    with LongCodec.LittleEndianEncoder with LongCodec.LittleEndianDecoder

  val values = BigEndian :: LittleEndian :: Nil

  def byName(name: String): Option[ByteOrder] =
    values.find(_.name == name)

  def byJavaValue(value: java.nio.ByteOrder): ByteOrder =
    if (value == BigEndian.javaValue) BigEndian else LittleEndian

}
