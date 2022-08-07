package shop.http.routes.auth

import cats.MonadThrow
import org.http4s.circe.JsonDecoder
import shop.services.AuthsService
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import shop.ext.http4s.refined
import org.http4s.circe._
import shop.domain.AuthDomain.*
import shop.domain.AuthDomain.LoginUser.*
import com.khanr1.auth.Jwt.JwtSecret.*
import org.http4s.circe.CirceEntityCodec.*

import cats.syntax.all.* 
import org.http4s.Response
import org.http4s.Status
import org.http4s.server.Router

final case class LoginRoutes[F[_]:MonadThrow:JsonDecoder](auth:AuthsService[F]) extends Http4sDsl[F]{
    private[routes] val prefix:String = "/auth"
    private[routes] val httpRoutes:HttpRoutes[F]= HttpRoutes.of[F]{
        case req @POST -> Root /"login" =>{
            req.asJsonDecode[LoginUser].flatMap{user =>
                auth.login(user.username.toDomain,user.password.toDomain)
                .flatMap(Ok(_))
                .recoverWith{
                    case UserNotFound(_) | InvalidPassword(_) => Forbidden()
                }
            }

            
        }
    }
    val routes:HttpRoutes[F]=Router{
        prefix->routes
    }
}
