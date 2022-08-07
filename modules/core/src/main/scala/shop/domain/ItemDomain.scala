package shop.domain

import java.util.UUID
import monocle.Iso
import io.circe.Encoder
import io.circe.Decoder
import squants.market._
import shop.domain.BrandDomain.*
import shop.domain.CategoryDomain.*
import io.circe.KeyDecoder
import io.circe.KeyEncoder
import shop.optics.IsUUID
import cats.Show
import shop.domain.CartDomain.CartItem
import shop.domain.CartDomain.Quantity


object ItemDomain {

  opaque type ItemID=UUID
  object ItemID{
      def apply(uuid:UUID):ItemID=uuid
      extension  (itemID:ItemID) def value:UUID=itemID

      given uuidIso:IsUUID[ItemID]=new IsUUID[ItemID]{
       def _UUID: Iso[UUID, ItemID] =  Iso[UUID,ItemID](apply)(_.value)
      }
      given encoder:Encoder[ItemID]= id => Encoder.encodeUUID.apply(id.value)
      given decoder:Decoder[ItemID]=Decoder.decodeUUID.map(apply)
      given keyDecoder:KeyDecoder[ItemID]=KeyDecoder.decodeKeyUUID.map(apply)
      given keyEncoder:KeyEncoder[ItemID]=KeyEncoder.encodeKeyUUID.contramap(_.value)
      given show:Show[ItemID]=Show.show(id=> id.toString)
    }

  opaque type ItemName=String
  object ItemName{
      def apply(str:String):ItemName=str
      extension (name:ItemName) def value:String =name

      given encoder:Encoder[ItemName]=name => Encoder.encodeString.apply(name.value)
      given decoder:Decoder[ItemName]=Decoder.decodeString.map(apply)

  }

  opaque type ItemDescription=String
  object ItemDescription{
      def apply(str:String):ItemDescription=str
      extension (name:ItemDescription) def value:String =name

      given encoder:Encoder[ItemDescription]=name => Encoder.encodeString.apply(name.value)
      given decoder:Decoder[ItemDescription]=Decoder.decodeString.map(apply)

  }
  
  final case class Item(
      uuid:ItemID,
      name:ItemName,
      description:ItemDescription,
      price: Money,
      brand:Brand,
      category:Category
  ){
    def cart(q:Quantity):CartItem = CartItem(this,q)
  }
  object Item{
    given encoder:Encoder[Item]=Encoder.forProduct6(
      "uuid",
      "name",
      "description",
      "price",
      "brand",
      "category"
    )(i => (i.uuid,i.name,i.description,i.price,i.brand,i.category))
    
    given decoder:Decoder[Item]=Decoder.forProduct6(
      "uuid",
      "name",
      "description",
      "price",
      "brand",
      "category"
    )(apply)
  }
  
  final case class UpdateItem(
    id:ItemID,
    price:Money
  )

  final case class CreateItem(
    name: ItemName,
    description:ItemDescription,
    price: Money,
    brandid:BrandID,
    categoryid:CategoryID
  )

  opaque type ItemNameParam=String
  object ItemNameParam{
    def apply(str:String):ItemNameParam=str
    extension (inp:ItemNameParam) def value:String=inp

    given decoder:Decoder[ItemNameParam]=Decoder.decodeString.map(apply)
    given encoder:Encoder[ItemNameParam]=Encoder.encodeString.contramap(_.value)
    
  }
  opaque type ItemDescriptionParam=String
  object ItemDescriptionParam{
    def apply(str:String):ItemDescriptionParam=str
    extension (inp:ItemDescriptionParam) def value:String=inp

    given decoder:Decoder[ItemDescriptionParam]=Decoder.decodeString.map(apply)
    given encoder:Encoder[ItemDescriptionParam]=Encoder.encodeString.contramap(_.value)

  }

  opaque type PriceParam=String
  object PriceParam{
    def apply(str:String):PriceParam=str
    extension (inp:PriceParam) def value:String=inp

    given decoder:Decoder[PriceParam]=Decoder.decodeString.map(apply)
    given encoder:Encoder[PriceParam]=Encoder.encodeString.contramap(_.value)

  }
  opaque type ItemIDParam=String
  object ItemIDParam{
    def apply(str:String):ItemIDParam=str
    extension (inp:ItemIDParam) def value:String=inp

    given decoder:Decoder[ItemIDParam]=Decoder.decodeString.map(apply)
    given encoder:Encoder[ItemIDParam]=Encoder.encodeString.contramap(_.value)

  }

  case class CreateItemParam(
    name:ItemNameParam,
    description:ItemDescriptionParam,
    price:PriceParam,
    brandId:BrandID,
    categoryId:CategoryID
  ){
    
    def toDomain:CreateItem=CreateItem(
      ItemName(name),
      ItemDescriptionParam(description),
      USD(BigDecimal(price)),
      brandId,
      categoryId
    )
  
  }

  object CreateItemParam{
    given encoder:Encoder[CreateItemParam]=Encoder.forProduct5("name","description","price","brandId","categoryId")(u=>(u.name,u.description,u.price,u.brandId,u.categoryId))
    given decoder:Decoder[CreateItemParam]=Decoder.forProduct5("name","description","price","brandId","categoryId")(apply)

  }

  case class UpdateItemParam(id:ItemIDParam,price:PriceParam){
    def toDomain:UpdateItem=UpdateItem(ItemID(UUID.fromString(id)),USD(BigDecimal(price)))
  }
  object UpdateItemParam{
    given encoder:Encoder[UpdateItemParam]=Encoder.forProduct2("id","price")(u=>(u.id,u.price))
    given decoder:Decoder[UpdateItemParam]=Decoder.forProduct2("id","price")(apply)

  }






}
