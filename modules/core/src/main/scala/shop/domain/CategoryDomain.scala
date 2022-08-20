package shop.domain

import java.util.UUID
import io.circe.Encoder
import io.circe.Decoder
import monocle.Iso
import cats.arrow.Category
import shop.optics.IsUUID
import cats.Show
import cats.kernel.Eq

object CategoryDomain {
    opaque type CategoryID=UUID
    object CategoryID{
        def apply(id:UUID):CategoryID=id
        extension (categoryid:CategoryID) def value:UUID=categoryid

        given encoder:Encoder[CategoryID]=categoryid=> Encoder.encodeUUID.apply(categoryid.value)
        given decoder:Decoder[CategoryID]=Decoder.decodeUUID.map(apply)
        given eq:Eq[CategoryID]=Eq.fromUniversalEquals
        given show:Show[CategoryID]=Show.show(id=> id.toString())
        given uuidIso: IsUUID[CategoryID]=new  IsUUID[CategoryID]{
            def _UUID: Iso[UUID,CategoryID]=Iso[UUID,CategoryID](apply)(_.value)   
        }
    }
    opaque type CategoryName=String
    object CategoryName{
        def apply(str:String):CategoryName=str
        extension (name:CategoryName) def value:String =name

        given encoder:Encoder[CategoryName]=name => Encoder.encodeString.apply(name.value)
        given decoder:Decoder[CategoryName]=Decoder.decodeString.map(apply)
        given show :Show[CategoryName]=Show.fromToString
    }

    opaque type CategoryParam=String
    object CategoryParam{
        def apply(str:String):CategoryParam= str
        extension (cp:CategoryParam) {
            def value:String=cp
            def toDomain:CategoryName=CategoryName(cp.value)
        }

        given encoder:Decoder[CategoryParam]=Decoder.forProduct1("name")(apply)

        
    }

    final case class Category(id:CategoryID,name:CategoryName)
    object Category{
        given encoder:Encoder[Category]=Encoder.forProduct2("id","name")(c=>(c.id,c.name))
        given decoder:Decoder[Category]=Decoder.forProduct2("id","name")(apply)
        given show:Show[Category]=Show.fromToString
    }
  
}
