package shop.domain

import shop.domain.AuthDomain.UserID
import squants.market.Money
import shop.domain.CheckOutDomain.*
import io.circe.Encoder
import io.circe.Decoder
import cats.Show

object PaymentDomain {
  case class Payment(id:UserID,total:Money,card:Card)
  object Payment{
    given encoder:Encoder[Payment]=Encoder.forProduct3("id","total","card")(u=> (u.id,u.total,u.card))
    given decoder:Decoder[Payment]=Decoder.forProduct3("id","total","card")(apply)
    given show:Show[Payment]=Show.fromToString
  }
  
}
