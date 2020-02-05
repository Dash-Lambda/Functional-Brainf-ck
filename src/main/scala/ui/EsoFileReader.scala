package ui

import common.{EsoExcep, EsoObj}

import scala.io.Source
import scala.util.{Failure, Success, Try}

object EsoFileReader extends EsoObj{
  val encodings: LazyList[String] = LazyList("UTF-8", "Cp1252", "UTF-16")
  
  def getSource(fnam: String): Option[Try[String]] = encodings
    .map(e => readFile(fnam, e))
    .collectFirst {
      case s: Success[String] => s
      case Failure(ex: java.io.FileNotFoundException) => Failure(ex)}
  
  def readFile(fnam: String): Try[String] = {
    getSource(fnam)
      .getOrElse(Failure(EsoExcep("Incompatible File Encoding")))}
  def readFile(fnam: String, enc: String): Try[String] = Try{
    val src = Source.fromFile(fnam, enc)
    val res = src.mkString.replaceAllLiterally("\r\n", "\n")
    src.close()
    res}
}