package shop.http.routes.admin

import cats.MonadThrow
import cats.syntax.all.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import shop.domain.AuthDomain.*
import shop.domain.ItemDomain.*
import shop.http.auth.UserAuth.AdminUser
import shop.services.ItemsService
import io.circe.JsonObject
import io.circe.syntax.*
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.AuthMiddleware
import org.http4s.AuthedRoutes


final case class AdminItemRoutes[F[_]:MonadThrow:JsonDecoder](items:ItemsService[F]) extends Http4sDsl[F]{
    private[admin] val prefixPath:String="/items"
    private[admin] val service:AuthedRoutes[AdminUser,F]= AuthedRoutes.of{
        //create item
        case ar @ POST -> Root as _ => ar.req.asJsonDecode[CreateItemParam]
                                                       .flatMap(c=>items.create(c.toDomain))
                                                       .flatMap(id=>Created(JsonObject.singleton("id",id.asJson)))
        //update price
        case ar @ POST -> Root as _ => ar.req.asJsonDecode[UpdateItemParam].flatMap(c=> items.update(c.toDomain))
                                                                                     .flatMap(id=>Created(JsonObject.singleton("id",id.asJson)))
    }

    def routes(auth:AuthMiddleware[F ,AdminUser]):HttpRoutes[F]=Router{
        prefixPath-> auth(service)
    }
}
