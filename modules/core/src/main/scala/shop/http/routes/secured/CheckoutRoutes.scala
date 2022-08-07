package shop.http.routes.secured

import cats.MonadThrow
import cats.syntax.all.*
import io.circe.Decoder
import org.http4s.circe.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.http4s.Request
import org.http4s.Response
import org.http4s.server.Router
import shop.domain.AuthDomain.UserID
import shop.domain.CartDomain.*
import shop.domain.CheckOutDomain.Card
import shop.domain.OrderDomain.EmptyCartError
import shop.domain.OrderDomain.OrderOrPaymentError
import shop.http.auth.UserAuth.CommonUser
import shop.programs.Checkout
import org.http4s.AuthedRequest.apply
import org.http4s.AuthedRoutes
import org.http4s.server.AuthMiddleware


final case class CheckoutRoutes[F[_]:JsonDecoder:MonadThrow](checkout:Checkout[F]) extends Http4sDsl[F]{
    private[routes] val prefixPath:String="/checkout"
    
    extension (req:Request[F]){
        def decodeR[A:Decoder](f:A=>F[Response[F]]):F[Response[F]]=
            req.asJsonDecode[A].attempt.flatMap{
                case Left(e)  => UnprocessableEntity()
                case Right(a) => f(a)
            }
    }
    

    private[routes] val service:AuthedRoutes[CommonUser,F]= AuthedRoutes.of{
        case ar @POST -> Root as user =>ar.req.decodeR[Card]{ 
            card => checkout
                    .process(user.value.id,card)
                    .flatMap(Created(_))
                    .recoverWith{
                        case CartNotFound(userId)=> NotFound(s"Cart not found for user: ${userId.value}")
                        case EmptyCartError =>BadRequest("Shopping cart is Empty")
                        case e :OrderOrPaymentError=>BadRequest(e.toString)
                    }


        }   
    }

    def  routes(auth:AuthMiddleware[F,CommonUser]):HttpRoutes[F]=Router{
        prefixPath-> auth(service)
    }

}
