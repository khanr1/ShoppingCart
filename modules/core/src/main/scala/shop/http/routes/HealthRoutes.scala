package shop.http.routes

import cats.Monad
import shop.services.HealthCheckServices
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.circe.CirceEntityEncoder.*

final case class HealthRoutes[F[_]:Monad](health:HealthCheckServices[F]) extends Http4sDsl[F]{
    private[routes] val prefixPath:String="/healthcheck"
    private[routes] val httpRoutes:HttpRoutes[F]=HttpRoutes.of[F]{
        case GET -> Root => Ok(health.status)
    }
    val routes:HttpRoutes[F]= Router{
        prefixPath-> httpRoutes
    }
}
