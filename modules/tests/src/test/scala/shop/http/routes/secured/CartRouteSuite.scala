package shop.http.routes.secured


import cats.data.Kleisli
import cats.effect.IO
import org.http4s._
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.client.dsl.io.*
import org.http4s.Method._
import org.http4s.server.*
import org.http4s.server.AuthMiddleware
import org.http4s.syntax.literals.*
import shop.domain.AuthDomain.*
import shop.domain.CartDomain.*
import shop.domain.ItemDomain.*
import shop.Generators.*
import shop.http.auth.UserAuth.CommonUser
import shop.services.ShoppingCartsService
import squants.market.Currency.apply
import squants.market.USD
import suite.HttpSuite

object CartRouteSuite extends HttpSuite{
  def authMiddleware(
    authUser:CommonUser
  ): AuthMiddleware[IO,CommonUser]=AuthMiddleware(Kleisli.pure(authUser))

  def dataCart(cartTotal:CartTotal)= new TestCart {
    override def get(userId: UserID): IO[CartTotal] =
      IO.pure(cartTotal)
  }

  test("Get shopping cart suceeds"){
    val gen= for{
        u<-commonUserGen
        c<-cartTotalGen
    } yield u->c
    forall(gen){
        case (user,ct)=>
            val req= GET(uri"/cart")
            val routes=CartRoutes[IO](dataCart(ct)).routes(authMiddleware(user))
            expectHttpBodyAndStatus(routes,req)(ct,Status.Ok)
    }
  }

  test("POST add item to shopping carts succeeds"){
    val gen =for{
        u<- commonUserGen
        c<- cartGen
    } yield u -> c

    forall(gen){
        case(user,c) =>
            val req = POST(c,uri"/cart")
            val routes= CartRoutes[IO](new TestCart).routes(authMiddleware(user))
            expectHttpStatus(routes,req)(Status.Created)

    }
  }






}

protected class TestCart extends ShoppingCartsService[IO]{
    override def add(userId: UserID, itemId: ItemID, quantity: Quantity): IO[Unit] = IO.unit
    override def delete(userId: UserID): IO[Unit] = IO.unit
    override def get(userId: UserID): IO[CartTotal] = IO.pure(CartTotal(List.empty,USD(0)))
    override def removeItem(userId: UserID, itemId: ItemID): IO[Unit] = IO.unit
    override def update(userId: UserID, cart: Cart): IO[Unit] = IO.unit
}