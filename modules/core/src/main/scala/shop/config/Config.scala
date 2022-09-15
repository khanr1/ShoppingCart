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

  // Ciris promotes configuration as code
  def load[F[_]: Async]: F[AppConfig] =
    env("SC_APP_ENV")
      .as[AppEnvironment]
      .flatMap {
        case Test =>
          default[F](
            RedisURI("redis://localhost"),
            PaymentURI("https://payments.free.beeceptor.com")
          )
        case Prod =>
          default[F](
            RedisURI("redis://10.123.154.176"),
            PaymentURI("https://payments.net/api")
          )
      }
      .load[F]

  private def default[F[_]](
      redisUri: RedisURI,
      paymentUri: PaymentURI
  ): ConfigValue[F, AppConfig] =
    (
      env("SC_JWT_SECRET_KEY").as[String].map(JwtSecretKeyConfig(_)).secret,
      env("SC_JWT_CLAIM").as[String].map(x=> JwtClaimConfig(x)).secret,
      env("SC_ACCESS_TOKEN_SECRET_KEY").as[String].map(JwtAccessTokenKeyConfig(_)).secret,
      env("SC_ADMIN_USER_TOKEN").as[String].map(AdminUserTokenConfig(_)).secret,
      env("SC_PASSWORD_SALT").as[String].map(PasswordSalt(_)).secret,
      env("SC_POSTGRES_PASSWORD").as[String].secret
    ).parMapN { (jwtSecretKey, jwtClaim, tokenKey, adminToken, salt, postgresPassword) =>
      AppConfig(
        AdminJwtConfig(jwtSecretKey, jwtClaim, adminToken),
        tokenKey,
        salt,
        TokenExpiration(30.minutes),
        ShoppingCartExpiration(30.minutes),
        CheckOutConfig(
          retriesLimit = 3,
          retriesBackoff = 10.milliseconds
        ),
        PaymentConfig(paymentUri),
        HttpClientConfig(
          timeout = 60.seconds,
          idleTimeInPool = 30.seconds
        ),
        PostgresSQLConfig(
          host = "localhost",
          port = 5432,
          user = "postgres",
          password = postgresPassword,
          database = "store",
          max = 10
        ),
        RedisConfig(redisUri),
        HttpServerConfig(
          host = host"0.0.0.0",
          port = port"8080"
        )
      )
    }

}

