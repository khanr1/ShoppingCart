package shop.http.routes.secured

import cats.Monad
import shop.services.OrderService
import org.http4s.dsl.Http4sDsl

import shop.http.auth.UserAuth.CommonUser

import shop.domain.AuthDomain.UserID

import shop.http.vars.Vars.OrderIDVar
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.server.Router
import org.http4s.AuthedRoutes
import org.http4s.server.AuthMiddleware

final case class OrderRoutes[F[_]:Monad](orders:OrderService[F]) extends Http4sDsl[F]{
    private[routes] val prefixPath:String="/orders"
    private[routes] val service:AuthedRoutes[CommonUser,F]=AuthedRoutes.of{
        case GET -> Root as user =>
            Ok(orders.findBy(user.value.id))
        case GET -> Root /OrderIDVar(orderId) as user =>
            Ok(orders.get(user.value.id,orderId))
    }
    def routes(auth:AuthMiddleware[F,CommonUser]):HttpRoutes[F]=Router{
        prefixPath -> (auth(service))
    }
}
