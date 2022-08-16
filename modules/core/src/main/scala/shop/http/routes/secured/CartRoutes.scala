package shop.http.routes.secured

import cats.Monad
import cats.syntax.all.*
import org.http4s.AuthedRoutes
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.ErrorAction.httpRoutes
import shop.domain.AuthDomain.UserID
import shop.domain.CartDomain.Cart
import shop.http.auth.UserAuth.CommonUser
import shop.services.ShoppingCartsService

import shop.http.vars.Vars.ItemIDVar
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.AuthMiddleware.apply
import org.http4s.server.AuthMiddleware



final case class CartRoutes[F[_]:JsonDecoder:Monad](carts:ShoppingCartsService[F]) extends Http4sDsl[F]{
    private[routes] val prefixPath="/cart"
    private[routes] val service:AuthedRoutes[CommonUser, F]=AuthedRoutes.of{
        //Get Shopping cart
        case GET -> Root as user => Ok(carts.get(user.value.id))
        //Add Item to the cart 
        case ar @ POST -> Root as user =>
            ar.req.asJsonDecode[Cart].flatMap{c=>c.items.map{
                case (id,quantity) => carts.add(user.value.id,id,quantity)
            }.toList.sequence *> Created()
        }
        //modify items in Cart
        case ar @ PUT -> Root as user =>{
            ar.req.asJsonDecode[Cart].flatMap{c=>
                carts.update(user.value.id,c)*> Ok()}
        }
        //remove item from the cart
        case DELETE -> Root / ItemIDVar(itemId) as user => carts.removeItem(user.value.id, itemId) *> NoContent()
    }
    def routes(authMiddleware:AuthMiddleware[F,CommonUser]):HttpRoutes[F]=Router{
        prefixPath -> authMiddleware(service)
    }

}