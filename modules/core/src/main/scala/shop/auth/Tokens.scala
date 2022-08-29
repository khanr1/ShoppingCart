package shop.auth

import cats.*
import cats.syntax.all.*
import com.khanr1.auth.Jwt.*
import io.circe.syntax.*
import pdi.jwt.*
import shop.config.Types.*
import shop.effects.GenUUID

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
          claim <- jwtExpire.expiresIn(JwtClaim(s"{\"id\":\"${uuid}\"}"), exp)
          secretKey = JwtSecret(config.secret)
          token <- jwtEncode[F](claim, secretKey, JwtAlgorithm.HS256)
        } yield token
    }
}