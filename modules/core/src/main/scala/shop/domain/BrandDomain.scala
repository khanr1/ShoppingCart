package shop.domain

import java.util.UUID
import io.circe.Encoder
import io.circe.Decoder
import shop.optics.IsUUID
import monocle.Iso
import io.circe.Json
import cats.kernel.Eq
import cats.Show
import org.http4s.QueryParamDecoder


object BrandDomain {

  opaque type BrandID = UUID
  object BrandID{  
      def apply(uuid:UUID):BrandID=uuid
      extension (brandID:BrandID) def value:UUID=brandID

      given encoder:Encoder[BrandID]=brandID=> Encoder.encodeUUID.apply(brandID.value)
      given decoder:Decoder[BrandID]=Decoder.decodeUUID.map(apply)
      given showID:Show[BrandID]=Show.fromToString
      given eqID:Eq[BrandID]=new Eq[BrandID]{
        def eqv(x:BrandID,y:BrandID) = x.value==y.value
      }
      given uuidIso: IsUUID[BrandID] = new IsUUID[BrandID]{
        def _UUID: Iso[UUID,BrandID] = Iso[UUID,BrandID](apply)(_.value)
      }
      
  }
  
  opaque type BrandName= String
  object BrandName{
    def apply(name:String):BrandName= name
    extension (brandname:BrandName) def value:String= brandname

    given encoder:Encoder[BrandName]= brandname => Encoder.encodeString.apply(brandname.value)
    given decoder:Decoder[BrandName]= Decoder.decodeString.map(apply)
    given eqBrandName:Eq[BrandName]= Eq.fromUniversalEquals
    given showName:Show[BrandName]=Show.fromToString

  }

  final case class Brand(id:BrandID,name:BrandName)
  object Brand{
    given encoder:Encoder[Brand]=Encoder.forProduct2("id","name")(brand=>(brand.id,brand.name))
    given decoder:Decoder[Brand]=Decoder.forProduct2("id","name")(apply)
    given eqBrand:Eq[Brand]=Eq.fromUniversalEquals
    given showName:Show[Brand]=Show.fromToString

  }

  opaque type BrandParam=String
  object BrandParam{
    def apply(str:String):BrandParam=str
    extension (br:BrandParam) 
      def value:String =br
      def toDomain:BrandName=BrandName(br.value.toLowerCase.capitalize)

    given encoder:Encoder[BrandParam]=Encoder.encodeString.contramap(_.value)
    given decoder:Decoder[BrandParam]=Decoder.decodeString.map(apply)
    given queryParaDecoder:QueryParamDecoder[BrandParam]=QueryParamDecoder.stringQueryParamDecoder.map(_.value)

  }
}
