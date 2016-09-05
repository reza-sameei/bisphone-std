package com.bisphone.util

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.reflect.ClassTag


/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */
trait Convertor[I,O] {

  def title: String

  def unsafe(i:I):O

}

object Convertor {

  def apply[I,O](t: String)( f: I => O ): Convertor[I,O] = new Convertor[I,O] {
    override val title = t
    final override def unsafe(i:I):O = f(i)
  }
}

trait Convertors {

  def default[T:ClassTag] = {
    val c = implicitly[ClassTag[T]]
    Convertor[T,T](s"${c.runtimeClass.getName} => ${c.runtimeClass.getName}")(any => any)
  }

  implicit val string2string = default[String]

  implicit val string2int = Convertor[String,Int]("String => Int")(i => i.toInt)

  implicit val string2long = Convertor[String,Long]("String => Long")(i => i.toLong)

  implicit val string2finiteduration = Convertor[String,FiniteDuration]("String => FiniteDuration")(i => Duration(i).asInstanceOf[FiniteDuration])

}

object Convertors extends Convertors
