package parsers

import scala.util.control.TailCalls.{TailRec, done, tailcall}

class EsoLongestMatchParser[+A](parser1: => EsoParser[A], parser2: => EsoParser[A]) extends EsoParser[A] {
  private lazy val p = parser1
  private lazy val q = parser2
  
  def apply(inp: String): EsoParseRes[A] = applyByTramp(inp)
  
  override def tramp[AA >: A, B](inp: EsoParserInput, start_ind: Int)(cc: ParserContinuation[AA, B]): TailRec[ParseTrampResult[B]] = {
    tailcall(
      p.tramp(inp, start_ind)(
        pres =>
          tailcall(
            q.tramp(inp, start_ind)(
              qres =>
                if(pres.length >= qres.length && pres.passed) tailcall(cc(pres) flatMap (res => if(res.passed) done(res) else tailcall(cc(qres))))
                else tailcall(cc(qres) flatMap (res => if(res.passed) done(res) else tailcall(cc(pres))))))))}
}
object EsoLongestMatchParser{
  def apply[A](p: => EsoParser[A], q: => EsoParser[A]): EsoLongestMatchParser[A] = new EsoLongestMatchParser(p, q)
}