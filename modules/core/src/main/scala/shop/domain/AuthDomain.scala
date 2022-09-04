package shop.domain


import io.circe.{Encoder,Decoder}
import javax.crypto.EncryptedPrivateKeyInfo
import java.util.UUID
import shop.optics.IsUUID
import monocle.Iso
import scala.util.control.NoStackTrace
import cats.Show
import javax.crypto.Cipher
import cats.kernel.Eq



object AuthDomain {
  opaque type UserName=String
  object UserName{
      def apply(name:String):UserName=name
      extension  (username:UserName) def value:String=username

      given decoder:Decoder[UserName]=Decoder.decodeString.map(apply)
      given encoder:Encoder[UserName]=Encoder.encodeString.contramap(_.value)
      given show:Show[UserName]=Show.show(x=> x)
      given eq:Eq[UserName]=Eq.fromUniversalEquals
    }

  opaque type Password =String
  object Password{
      def  apply(str:String):Password=str
      extension (pass:Password) def value:String=pass

      given decoder:Decoder[Password]=Decoder.decodeString.map(apply)
      given encoder:Encoder[Password]=Encoder.encodeString.contramap(_.value)
      given show:Show[Password]=Show.show(x => x)
    }

  opaque type EncryptedPassword=String
  object EncryptedPassword{
      def apply(str:String):EncryptedPassword=str
      extension (ep:EncryptedPassword) def value:String=ep

      given decoder:Decoder[EncryptedPassword]=Decoder.decodeString.map(apply)
      given encoder:Encoder[EncryptedPassword]=Encoder.encodeString.contramap(_.value)
      given show:Show[EncryptedPassword]=Show.show(x=>x)

  }
  opaque type UserID =UUID
  object UserID{
      def apply(uuid:UUID):UserID=uuid
      extension (id:UserID) def value:UUID=id

      given encoder:Encoder[UserID]=Encoder.encodeUUID.contramap(_.value)
      given decoder:Decoder[UserID]=Decoder.decodeUUID.map(apply)
      given isUUID:IsUUID[UserID]=new IsUUID[UserID]{
          def _UUID:Iso[UUID,UserID]=Iso(apply)(u=> u.value)
      }
      given show:Show[UserID]=Show.show(uuid=>uuid.value.toString)
      given eq  :Eq[UserID] = Eq.fromUniversalEquals
  }

  opaque type EncryptCipher= Cipher
  object EncryptCipher{
    def apply(cp:Cipher):EncryptCipher=cp
    extension (ecp:EncryptCipher){
      def value:Cipher=ecp
    }
  }
  opaque type DecryptCipher= Cipher
  object DecryptCipher{
    def apply(cp:Cipher):DecryptCipher=cp
    extension (ecp:DecryptCipher){
      def value:Cipher=ecp
    }
  }
  final case class LoginUser(username:UserNameParam,password:PasswordParam)
  object LoginUser{
    given encoder: Encoder[LoginUser]=Encoder.forProduct2("username","password")(u=>(u.username,u.password))
    given decoder: Decoder[LoginUser]=Decoder.forProduct2("username","password")(apply)

  }
  // User registration //
  opaque type UserNameParam= String
  object UserNameParam{
    def apply(str:String):UserNameParam=str
    extension (unp:UserNameParam) {
      def value:String= unp
      def toDomain:UserName=UserName(unp.value)
    }

    given encoder:Encoder[UserNameParam]=Encoder.encodeString.contramap( _.value)
    given decoder:Decoder[UserNameParam]=Decoder.decodeString.map( apply)

  }
  opaque type PasswordParam= String
  object PasswordParam{
    def apply(str:String):PasswordParam=str
    extension (unp:PasswordParam) 
      def value:String= unp
      def toDomain:Password=PasswordParam(unp.value)

    given encoder:Encoder[PasswordParam]=Encoder.encodeString.contramap( _.value)
    given decoder:Decoder[PasswordParam]=Decoder.decodeString.map( apply)

  }
  case class CreateUser(username:UserNameParam,password:PasswordParam)
  object CreateUser{
    given encoder:Encoder[CreateUser]=Encoder.forProduct2("username","password")(u=>(u.username,u.password))
    given decoder:Decoder[CreateUser]=Decoder.forProduct2("username","password")(apply)
    
  }

  case class UserNotFound(username:UserName) extends NoStackTrace
  case class UserNameInUser(username:UserName) extends NoStackTrace
  case class InvalidPassword(username:UserName) extends NoStackTrace
  case object UnSupportedOperation  extends NoStackTrace
  
  // Adming Auth
  opaque type ClaimContent = UUID
  object ClaimContent{
    def apply(uuid:UUID):ClaimContent=uuid
    extension (claim:ClaimContent) def uuid:UUID=claim

    given decoder:Decoder[ClaimContent]=Decoder.decodeUUID.at("uuid").map(ClaimContent.apply)

  }


}