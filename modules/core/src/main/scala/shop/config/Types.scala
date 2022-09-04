package shop.config

import scala.concurrent.duration.FiniteDuration
import cats.Show
import ciris.ConfigDecoder
import ciris.Secret
import dev.profunktor.redis4cats.Redis
import com.comcast.ip4s.{Host,Port}

object Types {

  opaque type JwtSecretKeyConfig = String
  object JwtSecretKeyConfig{
    def apply(secret:String):JwtSecretKeyConfig=secret
    extension (jskc:JwtSecretKeyConfig) def secret:String= jskc
    given show:Show[JwtSecretKeyConfig]=Show.fromToString
    given configDecoder:ConfigDecoder[String,JwtSecretKeyConfig]=
      ConfigDecoder[String,JwtSecretKeyConfig].mapOption("JwtSecretKeyConfig")( x=> x match
        case x if x.secret.nonEmpty => Some(x)
        case _ => None
      )
  }

  opaque type JwtClaimConfig = String
  object JwtClaimConfig{
    def apply(str:String):JwtClaimConfig=str
    extension (jskc:JwtClaimConfig) def value:String= jskc
    given show:Show[JwtClaimConfig]=Show.fromToString
    given configDecoder:ConfigDecoder[String,JwtClaimConfig]=
      ConfigDecoder[String,JwtClaimConfig].mapOption("JwtClaimConfig")( x=> x match
        case x if x.nonEmpty => Some(x)
        case _ => None
      )
  }

  opaque type ShoppingCartExpiration = FiniteDuration
  object ShoppingCartExpiration{
    def apply(fd:FiniteDuration):ShoppingCartExpiration=fd
    extension (se:ShoppingCartExpiration) def value:FiniteDuration=se
  }
  opaque type TokenExpiration= FiniteDuration
  object TokenExpiration{
    def apply(fd:FiniteDuration):TokenExpiration=fd
    extension (te:TokenExpiration){
      def value:FiniteDuration=te
    }
  }
  opaque type PasswordSalt= String
  object PasswordSalt{
    def apply(str:String):PasswordSalt=str
    extension (ps:PasswordSalt){
      def value:String=ps
    }
    given show:Show[PasswordSalt]=Show.show(ps=> ps.value)
    given configDecoder:ConfigDecoder[String,PasswordSalt]=ConfigDecoder[String,PasswordSalt].map(apply)
      
  }
  opaque type JwtAccessTokenKeyConfig= String
  object JwtAccessTokenKeyConfig{
    def apply(str:String):JwtAccessTokenKeyConfig=str
    extension (ps:JwtAccessTokenKeyConfig){
      def secret:String=ps
    }
    given show:Show[JwtAccessTokenKeyConfig]=Show.show(ps=> ps.secret)
    given configDecoder:ConfigDecoder[String,JwtAccessTokenKeyConfig]=
      ConfigDecoder[String,JwtAccessTokenKeyConfig].map(apply)
      
  }

  opaque type AdminUserTokenConfig= String
  object AdminUserTokenConfig{
    def apply(str:String):AdminUserTokenConfig=str
    extension (ps:AdminUserTokenConfig){
      def secret:String=ps
    }
    given show:Show[AdminUserTokenConfig]=Show.show(ps=> ps.secret)
    given configDecoder:ConfigDecoder[String,AdminUserTokenConfig]=
      ConfigDecoder[String,AdminUserTokenConfig].map(apply)
      
  }

  opaque type PaymentURI=String
  object PaymentURI{
    def apply(str:String):PaymentURI=str
    extension (pc:PaymentURI){
      def  value:String=pc
    }
  }

  opaque type PaymentConfig=PaymentURI
  object PaymentConfig{
    def apply(puri:PaymentURI):PaymentConfig=puri
    extension (pc:PaymentConfig){
      def  uri:PaymentURI=pc
    }
  }

  case class CheckOutConfig(retriesLimit:Int,retriesBackoff:FiniteDuration)

  case class AppConfig(
    adminJwtConfig:AdminJwtConfig,
    tokenConfig:Secret[JwtAccessTokenKeyConfig],
    passwordSalt:Secret[PasswordSalt],
    tokenExpiration:TokenExpiration,
    cartExpiration:ShoppingCartExpiration,
    checkoutConfig:CheckOutConfig,
    paymentConfig:PaymentConfig,
    httpClientConfig:HttpClientConfig,
    postgresSQL:PostgresSQLConfig,
    redis:RedisConfig,
    httpServerConfig:HttpServerConfig
  )

  case class AdminJwtConfig(
      secretKey: Secret[JwtSecretKeyConfig],
      claimStr: Secret[JwtClaimConfig],
      adminToken: Secret[AdminUserTokenConfig]
  )

  opaque type RedisURI=String
  object RedisURI{
    def apply(str:String):RedisURI=str
    extension (ruri:RedisURI) def value:String=ruri
  }
  opaque type RedisConfig=RedisURI
  object RedisConfig{
    def apply(uri :RedisURI):RedisConfig=uri
    extension (rcfg:RedisConfig) def uri:RedisURI=rcfg
  }

  case class HttpServerConfig(
    host:Host,
    port:Port
  )

  case class HttpClientConfig(
    timeout:FiniteDuration,
    idleTimeInPool:FiniteDuration
  )

  case class PostgresSQLConfig(
    host:String,
    port:Int,
    user:String,
    password:Secret[String],
    database:String,
    max:Int
  )
  
}
