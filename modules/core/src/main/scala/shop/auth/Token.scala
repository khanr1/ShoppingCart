package shop.auth

import com.khanr1.auth.Jwt.JwtToken
import shop.effects.GenUUID
import cats.*
import cats.syntax.all.*
import pdi.jwt.*
import io.circe.syntax.*
import com.khanr1.auth.Jwt.*
import shop.config.Types.*

trait Tokens[F[_]] {
  def create: F[JwtToken]
}

object Tokens {
  def make[F[_]: GenUUID: Monad](
      jwtExpire: JwtExpire[F],
      config: JwtAccessTokenKeyConfig,
      exp: TokenExpiration
  ): Tokens[F] =
    new Tokens[F] {
      def create: F[JwtToken] =
        for {
          uuid  <- GenUUID[F].make
          claim <- jwtExpire.expiresIn(JwtClaim(uuid.asJson.noSpaces), exp)
          secretKey = JwtSecret(config.value)
          token <- jwtEncode[F](claim, secretKey, JwtAlgorithm.HS256)
        } yield token
    }
}