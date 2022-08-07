package shop.http.routes.auth

import shop.services.AuthsService
import cats.MonadThrow
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.http4s.circe._
import shop.domain.AuthDomain.CreateUser
import cats.syntax.all.* 
import org.http4s.circe.CirceEntityEncoder._
import shop.domain.AuthDomain.UserNameInUser
import org.http4s.server.Router

final case class UserRoutes[F[_]:MonadThrow:JsonDecoder](auth:AuthsService[F]) extends Http4sDsl[F]{
    private[routes] val prefixPath:String = "/auth"
    private[routes] val httpRoutes:HttpRoutes[F]=HttpRoutes.of[F]{
        case req @ POST -> Root /"users" =>
            req.asJsonDecode[CreateUser]
               .flatMap( user => auth.newUser(user.username.toDomain,user.password.toDomain))
               .flatMap(Created(_))
               .recoverWith{
                   case UserNameInUser(u) => Conflict(u.toString)
               }
    }
    val routes:HttpRoutes[F]=Router{
        prefixPath-> httpRoutes
    }

}
