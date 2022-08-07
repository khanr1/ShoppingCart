package shop.domain

import squants.Quantity
import io.circe.{Encoder,Decoder}
import shop.domain.ItemDomain.*
import io.circe.Json
import squants.market.Money
import shop.domain.AuthDomain.UserID
import scala.util.control.NoStackTrace
import cats.Show
import squants.market.USD


object CartDomain {

  opaque type Quantity=Int
  object Quantity{
      def apply(i:Int):Quantity=i
      extension (q:Quantity) def value:Int=q
      
      given encoder:Encoder[Quantity]=Encoder.encodeInt.contramap(_.value)
      given decoder:Decoder[Quantity]=Decoder.decodeInt.map(apply)
      given show:Show[Quantity]= Show.fromToString
  }

  opaque type Cart=Map[ItemID,Quantity]
  object Cart{
      def apply(m:Map[ItemID,Quantity]):Cart=m
      extension (c:Cart) def items:Map[ItemID,Quantity]=c

      given encoder:Encoder[Cart]=cart => Json.obj( "items" -> Encoder.encodeMap.apply(cart.items))
      given decoder:Decoder[Cart]=Decoder.decodeMap.at("items")  
    }
  
  case class CartItem(item:Item, quantity:Quantity){
    import Quantity.*
    def subtotal:Money=USD(item.price.amount* quantity.value)
  }
  object CartItem{
    
    given encoder:Encoder[CartItem]=Encoder.forProduct2("item","quantity")(c=> (c.item,c.quantity))
    given decoder:Decoder[CartItem]=Decoder.forProduct2("item","quantity")(apply)
  }

  case class CartTotal(items:List[CartItem],total:Money)
  object CartTotal{

    given encoder:Encoder[CartTotal]=Encoder.forProduct2("items","total")(c=>(c.items,c.total))
    given decoder:Decoder[CartTotal]=Decoder.forProduct2("items","total")(apply)
  }

  case class CartNotFound(userId:UserID) extends NoStackTrace
  object CartNotFound{
    given encoder:Encoder[CartNotFound]=Encoder.forProduct1("userId")(u=>(u.userId))
    given decoder:Decoder[CartNotFound]=Decoder.forProduct1("userId")(apply)

  }



}
