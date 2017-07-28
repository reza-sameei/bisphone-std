package com.bisphone.stdv1.util

import com.bisphone.stdv1.predef._

import scala.reflect.ClassTag

/**
  * @author Reza Samei <reza.samei.g@gmail.com>
  */

trait Convertors {

    def default[T:ClassTag] = {
        val c = implicitly[ClassTag[T]]
        Convertor[T,T](s"${c.runtimeClass.getName} => ${c.runtimeClass.getName}")(any => any)
    }

    implicit val std$convertor$string2string = default[String]

    implicit val std$convertor$string2int = Convertor[String,Int]("String => Int")(i => i.toInt)

    implicit val std$convertor$string2long = Convertor[String,Long]("String => Long")(i => i.toLong)

    implicit val std$convertor$string2float = Convertor[String,Float]("String => Float")(i => i.toFloat)

    implicit val std$convertor$string2double = Convertor[String,Double]("String => Float")(i => i.toDouble)

    implicit val std$convertor$string2bool = Convertor[String, Boolean]("String => Boolean")( i => i.toLowerCase match {
        case "true" | "on" => true
        case "false" | "off" => false
    })

    implicit val std$convertor$string2path = Convertor[String, Path.Type]("String => Path"){ raw =>
        java.nio.file.Paths.get(raw)
    }

    implicit val std$convertor$string2pathabsolute = Convertor[String, Path.Absolute]("String => Path.Absolute") { raw =>
        val path = java.nio.file.Paths.get(raw)
        if (path.isAbsolute) Path.Absolute(path)
        else throw new IllegalArgumentException(s"The path isn't absolute: ${path}")
    }

    implicit val std$convertor$string2pathreadablefile = Convertor[String, Path.ReadableFile]("String => Path.Absolute") { raw =>
        val path = java.nio.file.Paths.get(raw)
        val file = path.toFile
        if (file.isFile && file.canRead) Path.ReadableFile(path)
        else throw new IllegalArgumentException(s"The path don't refer to a file/readable-file: ${path}")
    }

    implicit val std$convertor$string2finiteduration = Convertor[String,FiniteDuration]("String => FiniteDuration")(i => Duration(i).asInstanceOf[FiniteDuration])

}

object Convertors extends Convertors
