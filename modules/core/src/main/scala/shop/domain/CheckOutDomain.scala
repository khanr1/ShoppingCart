package shop.domain


import cats.data._
import cats.syntax.*
import cats.* 
import cats.implicits.*
import io.circe.Decoder
import io.circe.Encoder

object CheckOutDomain {
  case class Card(
    name:CardName,
    number:CardNumber,
    expiration:CardExpiration,
    ccv:CardCVV
  )
  object Card{
    given encoder:Encoder[Card]=Encoder.forProduct4("name","number","expiration","cvv")(u=>(u.name,u.number,u.expiration,u.ccv))
    given decoder:Decoder[Card]=Decoder.forProduct4("name","number","expiration","cvv")(apply)
    given show:Show[Card]=Show.fromToString
    given eq:Eq[Card]=Eq.fromUniversalEquals

  }
  
  opaque type CardName = String
  object CardName{
    def unSafeApply(str:String):CardName=str
    def apply(str:String):ValidatedNec[String,CardName]=
      if(str.matches("^[a-zA-Z]+(([',. -][a-zA-Z ])?[a-zA-Z]*)*$")) str.validNec else "Card name cannot have special character".invalidNec
    extension (cn:CardName) {
      def value:String= cn
      
    }

    private def  apply2(srt:String):Either[String,CardName]=apply(srt).toEither.leftMap(_.show)

    given decoder:Decoder[CardName]=Decoder.decodeString.emap(apply2(_))
  }
  opaque type CardNumber = Long
  object CardNumber{
    def unSafeApply(l:Long):CardNumber=l
    def apply(l:Long):ValidatedNec[String,CardNumber]=
      val numberOfDigit:Int= if(l==0) 1 else math.log10(math.abs(l)).toInt +1
      if (numberOfDigit == 16) l.validNec else "Card number needs to have 16 digit".invalidNec
    
    private def apply2(l:Long):Either[String,CardNumber]=apply(l).toEither.leftMap(_.show)
    extension (cn:CardNumber){
      def value:Long=cn
    }
    given decoder:Decoder[CardNumber]=Decoder.decodeLong.emap(apply2)
  }

  opaque type CardExpiration=String
  object CardExpiration{
    def unSafeApply(str:String):CardExpiration=str
    def apply(str:String):ValidatedNec[String,CardExpiration]=
      if(str.length==4) str.validNec else "Card expiration format needs to be mmyy".invalidNec
    private def apply2(str:String):Either[String,CardExpiration]=apply(str).toEither.leftMap(_.show)
    extension (ce:CardExpiration)
      def value:String=ce

    given decoder:Decoder[CardExpiration]=Decoder.decodeString.emap(apply2)

  }

  opaque type CardCVV=Int
  object CardCVV{
    def unSafeApply(i:Int):CardCVV=i
    def apply(i:Int):ValidatedNec[String,CardCVV]=
      val numberOfDigit:Int= if(i==0) 1 else math.log10(math.abs(i)).toInt +1
      if (numberOfDigit==3) i.validNec else "CardCVV need to be a number of length 3".invalidNec
    private def apply2(i:Int):Either[String,CardCVV]=apply(i).toEither.leftMap(_.show)
    extension (cvv:CardCVV)
      def value:Int= cvv
    
    given decoder:Decoder[CardCVV]=Decoder.decodeInt.emap(apply2)
  }

  
 

}
