package shop.http.routes.auth

import shop.services.AuthsService
import org.http4s.dsl.Http4sDsl
import shop.http.auth.UserAuth.CommonUser
import shop.domain.AuthDomain.UserID
import cats.MonadThrow
import cats.syntax.all.* 
import org.http4s.server.Router
import org.http4s.HttpRoutes
import org.http4s.AuthedRoutes
import com.khanr1.auth.AuthHeaders
import org.http4s.server.AuthMiddleware

final case class LogoutRoutes[F[_]:MonadThrow](auth:AuthsService[F]) extends Http4sDsl[F]{
    private[routes] val prefixPath:String="/auth"
    private[routes] val service:AuthedRoutes[CommonUser,F]=AuthedRoutes.of{
        case ar @ POST -> Root /"logout" as user => {
            AuthHeaders.getBearerToken(ar.req)
            .traverse_(auth.logout(_,user.value.name))*> NoContent()
        }
    }
    def routes(auth:AuthMiddleware[F,CommonUser]):HttpRoutes[F]=Router{
        prefixPath->auth(service)
    }
    
}
