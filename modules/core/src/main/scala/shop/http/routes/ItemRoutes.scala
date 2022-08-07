package shop.http.routes

import cats.Monad
import shop.services.ItemsService
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder.*

import shop.domain.BrandDomain.BrandParam
import org.http4s.server.Router

final case class ItemRoutes[F[_]:Monad](items:ItemsService[F]) extends Http4sDsl[F]{
    private[routes] val prefixPath:String = "/items"

    object BrandQueryParam extends OptionalQueryParamDecoderMatcher[BrandParam]("brand")
    
    private[routes] val httpRoutes:HttpRoutes[F]=HttpRoutes.of[F]{
        case GET -> Root :? BrandQueryParam(brand)=>
            Ok(brand.fold(items.findAll)(b=>items.findBy(b.toDomain)))
    }

    val routes:HttpRoutes[F]=Router(
        prefixPath->httpRoutes
    )
}
