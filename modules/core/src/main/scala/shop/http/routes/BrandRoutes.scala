package shop.http.routes

import cats.Monad
import shop.services.BrandsService
import org.http4s.dsl.Http4sDsl
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.server.Router

final case class BrandRoutes[F[_]:Monad](brands:BrandsService[F]) extends Http4sDsl[F]{
    
    private[routes] val prefixPath:String="/brands"

    private[routes] val httpRoutes:HttpRoutes[F]=HttpRoutes.of[F]{
        case GET -> Root => Ok(brands.findall)
    }

    val routes:HttpRoutes[F]= Router( prefixPath->httpRoutes)
    
}
