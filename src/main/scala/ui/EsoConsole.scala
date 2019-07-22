package ui

import Compilers.{Compiler, BFCompiler}
import ConsoleHandlers._
import assemblers.{Assembler, WhiteSpaceAssembler}
import interpreters.{BFManaged, FracTran, FracTranpp, Interpreter, ScalaRun, TransInterp, WhiteSpace, WhiteSpaceSL}
import translators.{BFTranslator, FlufflePuff, Ook}

import scala.collection.mutable

object EsoConsole {
  val pointer: String = "Eso> "
  val welcomeText: String =
    """|Welcome to Eso, the functional esoteric language interpreter!
       |Type "help" for a list of commands.
       |""".stripMargin
  val defaultBindingFile: String = "userBindings.txt"
  val assemVec: Vector[Assembler] = Vector[Assembler](WhiteSpaceAssembler)
  val nativeTrans: Vector[BFTranslator] = Vector[BFTranslator](FlufflePuff, Ook)
  val interpVec: Vector[Interpreter] = Vector[Interpreter](FracTran, FracTranpp, ScalaRun, WhiteSpace, WhiteSpaceSL, BFManaged)
  val compVec: Vector[Compiler] = Vector[Compiler](BFCompiler)
  
  val defBools: Vector[(String, (Boolean, String))] = Vector[(String, (Boolean, String))](
    "log" -> (true, "determines whether output is shown during or after runtime"),
    "debug" -> (false, "show runtime information, such as stack and heap states"),
    "dynamicTapeSize" -> (false, "resize tape as needed for BF interpreters, eliminates memory limitations but reduces speed"),
    "powExp" -> (false, "toggle raw value vs. factor expansion representation of FracTran output"))
  val defNums: Vector[(String, (Int, String))] = Vector[(String, (Int, String))](
    "BFOpt" -> (2, "BrainFuck interpreter selection: 0=base, 1=optimized, 2=compiled"),
    "initTapeSize" -> (40000, "initial tape length for BF interpreters"),
    "outputMaxLength" -> (-1, "maximum size of output string for interpreters, useful for non-terminating programs, -1 = infinite"),
    "dbTim" -> (0, "Debug sleep time, slows down execution when debug is on. Currently supported by FracTran++, WhiteSpace, and WhiteSpaceSL"))
  
  private val userBindings: mutable.HashMap[String, Vector[String]] = mutable.HashMap[String, Vector[String]]()
  private val bools: mutable.HashMap[String, (Boolean, String)] = mutable.HashMap[String, (Boolean, String)]()
  private val nums: mutable.HashMap[String, (Int, String)] = mutable.HashMap[String, (Int, String)]()
  private val BFTranslators: mutable.HashMap[String, BFTranslator] = mutable.HashMap[String, BFTranslator]()
  private val interpreters: mutable.HashMap[String, Interpreter] = mutable.HashMap[String, Interpreter]()
  private val assemblers: mutable.HashMap[String, Assembler] = mutable.HashMap[String, Assembler]()
  private val compilers: mutable.HashMap[String, Compiler] = mutable.HashMap[String, Compiler]()
  
  def main(args: Array[String]): Unit = {
    setDefaults()
    println(welcomeText)
    consoleLoop()
  }
  
  def setDefaults(): Unit = {
    BFTranslators.clear()
    interpreters.clear()
    assemblers.clear()
    compilers.clear()
    userBindings.clear()
    bools.clear()
    nums.clear()
    
    BFTranslators ++= nativeTrans.map(t => (t.name, t))
    interpreters ++= (interpVec ++ nativeTrans.map(trans => TransInterp(trans, BFManaged))).map(interp => (interp.name, interp))
    assemblers ++= assemVec.map(asm => (asm.name, asm))
    compilers ++= compVec.map(comp => (comp.name, comp))
    userBindings ++= loadBindingsHandler(defaultBindingFile)
    bools ++= defBools
    nums ++= defNums
  }
  
  def addTrans(pair: (String, BFTranslator)): Unit = {
    BFTranslators += pair
    interpreters += ((pair._1, TransInterp(pair._2, BFManaged)))
  }
  
  def consoleLoop(): Unit = {
    var runChk = true
    
    while(runChk){
      val inp = grabStr(s"$pointer").split(" ").toVector
      
      userBindings.get(inp.head) match{
        case Some(udb) => execCommand(udb ++ inp.tail)
        case None => execCommand(inp)
      }
    }
    
    def execCommand(inp: Vector[String]): Unit = inp match{
      case "run" +: args => runHandler(interpreters, bools, nums)(args)
      
      case "compile" +: args => compileHandler(compilers, bools, nums)(args)
      
      case "assemble" +: args => assembleHandler(assemblers, bools("log")._1, rev = false)(args)
      case "disassemble" +: args => assembleHandler(assemblers, bools("log")._1, rev = true)(args)
      
      case "optimize" +: args => optimizeHandler(args, bools("debug")._1)
      
      case "translate" +: args => translationHandler(BFTranslators)(args)
      case "defineBFLang" +: _ => addTrans(langCreationHandler)
      case "loadBFLangs" +: args => for(p <- loadBFLangsHandler(args)) addTrans(p)
      case "saveBFLangs" +: args => bfLangSaveHandler(BFTranslators, nativeTrans)(args)
      case "syntax" +: args => syntaxHandler(BFTranslators)(args)
      
      case "bind" +: tok +: args => userBindings += ((tok, args))
      case "unbind" +: tok +: _ => userBindings -= tok
      case "clrBindings" +: _ => userBindings.clear
      case "loadBindings" +: args => userBindings ++= loadBindingsHandler(defaultBindingFile)(args)
      case "saveBindings" +: args => saveBindingsHandler(userBindings, defaultBindingFile)(args)
      case "listBindings" +: _ => listBindingsHandler(userBindings)
      
      case "set" +: args => println(setVarHandler(bools, nums)(args))
      case "defaults" +: _ => setDefaults()
      
      case "listLangs" +: _ => listLangsHandler(interpreters, BFTranslators, assemblers, compilers)
      case "listVars" +: _ => println(printVarsHandler(bools, nums))
      case "help" +: _ => println(helpText)
      
      case "exit" +: _ =>
        println("Closing...")
        runChk = false
      
      case _ => println("Invalid command.")
    }
  }
}