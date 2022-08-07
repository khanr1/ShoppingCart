package shop.http.routes

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import shop.services.CategoriesService
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.server.Router

final case class CategoryRoutes[F[_]:Monad](catergories:CategoriesService[F]) extends Http4sDsl[F]{
    private[routes] val prefixPath:String="/categories"

    private[routes] val httpRoutes:HttpRoutes[F]=HttpRoutes.of[F]{
        case GET -> Root => Ok(catergories.findall)
    }

    val routes:HttpRoutes[F]=Router{
        prefixPath ->httpRoutes
    }



}
