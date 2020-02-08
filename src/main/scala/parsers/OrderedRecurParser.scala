package parsers

import scala.annotation.tailrec

case class OrderedRecurParser[A, B](depth: Int, priority: Int = 0)(recur: A => Option[(A, Int, Int)])(collect: Seq[B] => B)(endParser: OrderedParser[A, B]) extends OrderedParser[A, B]{
  def apply(inp: A): EsoParseRes[A, B] = {
    trait Cont{
      def apply(x: B): Cont}
    object FinCont extends Cont{
      def apply(x: B): Cont = ResCont(x)}
    case class ResCont(res: B) extends Cont{
      def apply(x: B): Cont = ResCont(res)}
    case class RecCont(d: Int, ac: Vector[B], cc: Cont) extends Cont{
      def apply(x: B): Cont = {
        if(d > 1) RecCont(d - 1, ac :+ x, cc)
        else cc(collect(ac :+ x))}}
    
    @tailrec
    def pdo(src: A, len: Int, cc: Cont): EsoParseRes[A, B] = {
      recur(src) match{
        case Some((nxt, _, ne)) => pdo(nxt, len + ne, RecCont(depth, Vector(), cc))
        case None => endParser(src) match{
          case EsoParsed(res, rem, _, ne) => pdo(rem, len + ne, cc(res))
          case EsoParseFail => cc match{
            case ResCont(res) => EsoParsed(res, src, priority, len)
            case _ => EsoParseFail}}}}
    recur(inp) match{
      case Some((nxt, start, end)) => pdo(nxt, end, RecCont(depth, Vector(), FinCont)) match{
        case EsoParsed(res, rem, _, ne) => EsoParsed(res, rem, start, ne)
        case _ => EsoParseFail}
      case None => endParser(inp)}}
}