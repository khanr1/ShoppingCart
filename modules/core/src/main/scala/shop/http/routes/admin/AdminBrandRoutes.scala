package shop.http.routes.admin

import cats.MonadThrow
import cats.syntax.all.* 
import io.circe.*
import io.circe.syntax.*
import org.http4s.circe.* 
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.http4s.server.Router
import shop.domain.AuthDomain.UserID
import shop.domain.BrandDomain.BrandParam
import shop.http.auth.UserAuth.AdminUser
import shop.services.BrandsService
import org.http4s.AuthedRoutes
import org.http4s.server.AuthMiddleware



final case class AdminBrandRoutes[F[_]:MonadThrow:JsonDecoder](brands:BrandsService[F]) extends Http4sDsl[F]{
    private[admin] val prefixPath:String="/brands"
    private[admin] val services:AuthedRoutes[AdminUser,F]=AuthedRoutes.of{
        case ar @ POST -> Root as  _ => {
            ar.req.asJsonDecode[BrandParam].flatMap{
                bp => brands.create(bp.toDomain)
                          .flatMap(id=> Created(JsonObject.singleton("brandID",id.asJson)))
            }
        }
    }

    def routes(auth:AuthMiddleware[F ,AdminUser]):HttpRoutes[F]=Router{
        prefixPath -> auth(services)
    }

}
