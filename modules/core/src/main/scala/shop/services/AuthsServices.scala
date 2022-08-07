package shop.services

import cats.effect.kernel.Sync
import cats.MonadThrow
import cats.syntax.all.*
import dev.profunktor.redis4cats.RedisCommands
import shop.domain.AuthDomain.Password
import shop.domain.AuthDomain.UserID
import shop.domain.AuthDomain.UserName
import shop.http.auth.UserAuth.User
import cats.data.OptionT
import com.khanr1.auth.Jwt.*
import pdi.jwt.*
import cats.Functor
import shop.http.auth.UserAuth.*
import io.circe.parser.decode
import io.circe.syntax.*
import cats.Applicative
import shop.http.auth.*
import shop.config.Types.*
import shop.auth.*
import shop.domain.AuthDomain.UserNameInUser
import shop.domain.AuthDomain.UserNotFound
import shop.domain.AuthDomain.InvalidPassword



trait  AuthsService[F[_]]{


    def newUser(username:UserName,password:Password):F[JwtToken]
    def login(username:UserName,password:Password):F[JwtToken]
    def logout(t:JwtToken,username:UserName):F[Unit]
    
}

object AuthsService {
  def make[F[_]: MonadThrow](
      tokenExpiration: TokenExpiration,
      tokens: Tokens[F],
      users: UsersService[F],
      redis: RedisCommands[F, String, String],
      crypto: Crypto
  ): AuthsService[F] =
    new AuthsService[F] {

      private val TokenExpiration = tokenExpiration.value

      def newUser(username: UserName, password: Password): F[JwtToken] =
        users.find(username).flatMap {
          case Some(_) => UserNameInUser(username).raiseError[F, JwtToken]
          case None =>
            for {
              i <- users.create(username, crypto.encrypt(password))
              t <- tokens.create
              u = User(i, username).asJson.noSpaces
              _ <- redis.setEx(t.value, u, TokenExpiration)
              _ <- redis.setEx(username.value, t.value, TokenExpiration)
            } yield t
        }

      def login(username: UserName, password: Password): F[JwtToken] =
        users.find(username).flatMap {
          case None => UserNotFound(username).raiseError[F, JwtToken]
          case Some(user) if user.password != crypto.encrypt(password) =>
            InvalidPassword(user.name).raiseError[F, JwtToken]
          case Some(user) =>
            redis.get(username.value).flatMap {
              case Some(t) => JwtToken(t).pure[F]
              case None =>
                tokens.create.flatTap { t =>
                  redis.setEx(t.value, user.asJson.noSpaces, TokenExpiration) *>
                    redis.setEx(username.value, t.value, TokenExpiration)
                }
            }
        }

      def logout(token: JwtToken, username: UserName): F[Unit] =
        redis.del(token.show) *> redis.del(username.value).void

    }
}

trait UsersAuth[F[_],A]{
    def findUser(t:JwtToken)(claim:JwtClaim):F[Option[A]]
}

object UsersAuth{
    def common[F[_]:Functor](redis:RedisCommands[F,String,String]):UsersAuth[F,CommonUser]={
        new UsersAuth[F,CommonUser]{
            def findUser(t: JwtToken)(claim: JwtClaim): F[Option[CommonUser]] = 
                redis.get(t.value).map(o => o.flatMap(u => decode[User](u).toOption.map(CommonUser(_))))
        }
    }
    def admin[F[_]:Applicative](adminToken:JwtToken,adminUser:AdminUser):UsersAuth[F,AdminUser]= 
        new UsersAuth[F,AdminUser]{
         def findUser(t: JwtToken)(claim: JwtClaim): F[Option[AdminUser]] = (t==adminToken).guard[Option].as(adminUser).pure[F]
        } 
 
}


