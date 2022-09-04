package shop.modules

import cats.*
import cats.effect.*
import cats.syntax.all.*
import com.khanr1.auth.Jwt.*
import dev.profunktor.redis4cats.RedisCommands
import io.lettuce.core.protocol.RedisCommand
import pdi.jwt.JwtAlgorithm
import shop.config.Types.AppConfig
import shop.http.auth.UserAuth.*
import shop.services.AuthsService
import shop.services.UsersAuth
import shop.domain.AuthDomain.*
import shop.auth.*
import skunk.Session
import io.circe.parser.{ decode => jsonDecode }
import shop.auth.JwtExpire
import shop.services.UsersService
import shop.http.auth.UserAuth

sealed abstract class Security[F[_]] private(
    val auth:AuthsService[F],
    val adminAuth:UsersAuth[F,AdminUser],
    val userAuth:UsersAuth[F,CommonUser],
    val adminJwtAuth:AdminJwtAuth,
    val userJwtAuth :UserJwtAuth
) 

object Security {
  def make[F[_]: Sync](
      cfg: AppConfig,
      postgres: Resource[F, Session[F]],
      redis: RedisCommands[F, String, String]
  ): F[Security[F]] = {

    val adminJwtAuth: AdminJwtAuth =
      AdminJwtAuth(
        JwtAuth
          .hmac(
            cfg.adminJwtConfig.secretKey.value.secret,
            JwtAlgorithm.HS256
          )
      )

    val userJwtAuth: UserJwtAuth =
      UserJwtAuth(
        JwtAuth
          .hmac(
            cfg.tokenConfig.value.secret,
            JwtAlgorithm.HS256
          )
      )

    val adminToken = JwtToken(cfg.adminJwtConfig.adminToken.value.secret)

    for {
      adminClaim <- jwtDecode[F](adminToken, adminJwtAuth.value)
      content    <- ApplicativeThrow[F].fromEither(jsonDecode[ClaimContent](adminClaim.content))
      adminUser = AdminUser(User(UserID(content.uuid), UserName("admin")))
      tokens <- JwtExpire.make[F].map(Tokens.make[F](_, cfg.tokenConfig.value, cfg.tokenExpiration))
      crypto <- Crypto.make[F](cfg.passwordSalt.value)
      users     = UsersService.make[F](postgres)
      auth      = AuthsService.make[F](cfg.tokenExpiration, tokens, users, redis, crypto)
      adminAuth = UsersAuth.admin[F](adminToken, adminUser)
      usersAuth = UsersAuth.common[F](redis)
    } yield new Security[F](auth, adminAuth, usersAuth, adminJwtAuth, userJwtAuth) {}

  }
}