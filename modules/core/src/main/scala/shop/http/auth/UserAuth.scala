package shop.http.auth

import shop.domain.AuthDomain.*
import io.circe.{Encoder,Decoder}


object UserAuth {
  case class User(id:UserID,name:UserName)
  object User{
      given encoder:Encoder[User]=Encoder.forProduct2("id","name")(u=>(u.id,u.name))
      given decoder:Decoder[User]=Decoder.forProduct2("id","name")(apply)
  }

  case class UserWithPassword(id:UserID,name:UserName,password:EncryptedPassword)
  object UserWithPassword{
      given encoder:Encoder[UserWithPassword]=Encoder.forProduct3("id","name","password")(u=>(u.id,u.name,u.password))
      given decoder:Decoder[UserWithPassword]=Decoder.forProduct3("id","name","password")(apply)
  }

  opaque type CommonUser=User
  object CommonUser{
      def apply(u:User):CommonUser=u
      extension (cu:CommonUser) def value:User=cu
  }

  opaque type AdminUser=User
  object AdminUser{
      def apply(u:User):AdminUser=u
      extension (ad:AdminUser) def value:User=ad
  }

}
