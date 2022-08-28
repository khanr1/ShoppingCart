package shop.config

import ciris.*
import shop.config.Types.*
import shop.config.AppEnvironment.*
import shop.config.AppEnvironment.given
import cats.syntax.all.* 
import scala.concurrent.duration.*
import com.comcast.ip4s.*
import cats.effect.kernel.Async
import org.http4s.client.defaults


object Config {
  val jwtSecretKey:ConfigValue[Effect,Secret[JwtSecretKeyConfig]]=env("SC_JWT_SECRET_KEY").as[JwtSecretKeyConfig].secret
  val jwtClaim: ConfigValue[Effect, Secret[JwtClaimConfig]] =env("SC_JWT_CLAIM").as[JwtClaimConfig].secret
  val accessTokenSecrectKey:ConfigValue[Effect,Secret[JwtAccessTokenKeyConfig]]= env("SC_ACCESS_TOKEN_SECRET_KEY").as[JwtAccessTokenKeyConfig].secret
  val adminUserToken:ConfigValue[Effect,Secret[AdminUserTokenConfig]]=env("SC_ADMIN_USER_TOKEN").as[AdminUserTokenConfig].secret
  val passwordSalt:ConfigValue[Effect,Secret[PasswordSalt]]=env("SC_PASSWORD_SALT").as[PasswordSalt].secret
  val postgresPwd : ConfigValue[Effect,Secret[String]]=env("SC_POSTGRES_PASSWORD").as[String].secret

  private def default[F[_]](
    redisUri:RedisURI,
    paymentUri:PaymentURI
  ): ConfigValue[F,AppConfig] = 
    (jwtSecretKey,jwtClaim,accessTokenSecrectKey,adminUserToken,passwordSalt,postgresPwd).parMapN{
      (jwtSecretKey,jwtClaim,tokenKey,admingToken,salt,dbPassword)=>
        AppConfig(
          AdminJwtConfig(jwtSecretKey,jwtClaim,admingToken),
          tokenKey,
          salt,
          TokenExpiration(30.minutes),
          ShoppingCartExpiration(30.minutes),
          CheckOutConfig(
            retriesLimit = 3,
            retriesBackoff  = 10.milliseconds
          ),
          PaymentConfig(paymentUri),
          HttpClientConfig(
            timeout = 60.seconds,
            idleTimeInPool = 30.seconds,
          ),
          PostgresSQLConfig(
            host = "localhost",
            port = "5432",
            user = "bot",
            password= dbPassword,
            database= "store",
            max = 10
          ),
          RedisConfig(redisUri),
          HttpServerConfig(
            host = host"0.0.0.0",
            port = port"8080"
          )
        )


    }

  def load[F[_]:Async]:F[AppConfig]=
    env("SC_APP_ENV")
      .as[AppEnvironment]
      .flatMap{
        case Test => default[F](RedisURI("redis: //localhost"),PaymentURI("https: //payments.free.beeceptor.com"))
        case Prod => default[F](RedisURI("redis://10.123.154.176"),PaymentURI("https://payments.net/api"))
      }.load[F]
}

