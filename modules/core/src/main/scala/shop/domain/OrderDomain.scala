package shop.domain

import java.util.UUID
import shop.optics.IsUUID
import monocle.Iso
import io.circe.Decoder
import io.circe.Encoder
import shop.domain.ItemDomain.ItemID
import squants.market.Money
import shop.domain.CartDomain.Quantity
import scala.util.control.NoStackTrace
import cats.Show

object OrderDomain {

  opaque type OrderID=UUID
  object OrderID{
      def apply(uuid:UUID):OrderID = uuid 
      extension (id:OrderID) def value:UUID=id

      given iso:IsUUID[OrderID]=new IsUUID[OrderID]{
          def _UUID:Iso[UUID,OrderID]=Iso[UUID,OrderID](apply)(_.value)
      }
      given decoder:Decoder[OrderID]=Decoder.decodeUUID.map(apply)
      given encoder:Encoder[OrderID]=Encoder.encodeUUID.contramap(_.value)
      
  }

  opaque type PaymentID=UUID
  object PaymentID{
      def apply(uuid:UUID):PaymentID = uuid 
      extension (id:PaymentID) def value:UUID=id

      given iso:IsUUID[PaymentID]=new IsUUID[PaymentID]{
          def _UUID:Iso[UUID,PaymentID]=Iso[UUID,PaymentID](apply)(_.value)
      }
      given decoder:Decoder[PaymentID]=Decoder.decodeUUID.map(apply)
      given encoder:Encoder[PaymentID]=Encoder.encodeUUID.contramap(_.value)
      given show:Show[PaymentID]=Show.fromToString

  }
  
  case class Order(id:OrderID,pid:PaymentID,items:Map[ItemID,Quantity],total:Money)
  object Order{
      given encoder:Encoder[Order]=Encoder.forProduct4(
          "id",
          "pid",
          "items",
          "total"
      )(o=> (o.id,o.pid,o.items,o.total))
      given decoder:Decoder[Order]=Decoder.forProduct4(
          "id",
          "pid",
          "items",
          "total"
      )(apply)
  }

  case object EmptyCartError extends NoStackTrace
  
  enum OrderOrPaymentError extends NoStackTrace{
      def cause: String
      case OrderError(cause:String) extends OrderOrPaymentError
      case PaymentError(cause:String) extends OrderOrPaymentError
  }
}
