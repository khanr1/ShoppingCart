package shop.http.routes.admin

import cats.MonadThrow
import org.http4s.circe.*
import shop.services.CategoriesService
import org.http4s.dsl.Http4sDsl

import shop.http.auth.UserAuth.AdminUser
import shop.domain.CategoryDomain.*
import cats.syntax.all.*
import shop.domain.AuthDomain.UserID
import org.http4s.circe.CirceEntityEncoder.*
import io.circe.syntax.*
import io.circe.JsonObject

import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.AuthedRoutes
import org.http4s.server.AuthMiddleware

final case class AdminCategoryRoutes[F[_]:MonadThrow:JsonDecoder](categories:CategoriesService[F]) extends Http4sDsl[F]{
    private[admin] val prefixPath:String= "/categories"
    private[admin] val service:AuthedRoutes[AdminUser,F]=AuthedRoutes.of{
        case ar @ POST -> Root as  _  => ar.req.asJsonDecode[CategoryParam]
                                                        .flatMap( cat=> categories.create(cat.toDomain))
                                                        .flatMap( id => Created(JsonObject.singleton("categotyID",id.asJson)) )
    }
      def routes(auth:AuthMiddleware[F ,AdminUser]):HttpRoutes[F]=Router{
        prefixPath -> auth(service)
    }

}
