package shop.http.auth

import shop.domain.AuthDomain.*
import io.circe.{Encoder,Decoder}
import cats.Show
import com.khanr1.auth.Jwt.JwtAuth.JwtSymmetric


object UserAuth {

  opaque type UserJwtAuth=JwtSymmetric
  object UserJwtAuth{
    def apply(jwt:JwtSymmetric):UserJwtAuth=jwt
    extension (userjwt:UserJwtAuth) def value:JwtSymmetric=userjwt
  }
  opaque type AdminJwtAuth=JwtSymmetric
  object AdminJwtAuth{
    def apply(jwt:JwtSymmetric):AdminJwtAuth=jwt
    extension (userjwt:AdminJwtAuth) def value:JwtSymmetric=userjwt
  }

  case class User(id:UserID,name:UserName)
  object User{
      given encoder:Encoder[User]=Encoder.forProduct2("id","name")(u=>(u.id,u.name))
      given decoder:Decoder[User]=Decoder.forProduct2("id","name")(apply)
      given show:Show[User]=Show.fromToString
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
      given show:Show[CommonUser]=Show.fromToString
  }

  opaque type AdminUser=User
  object AdminUser{
      def apply(u:User):AdminUser=u
      extension (ad:AdminUser) def value:User=ad
  }

}
