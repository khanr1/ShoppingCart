package shop.programs

import cats.data.NonEmptyList
import cats.effect.IO
import retry.RetryPolicies.*
import retry.RetryPolicy
import shop.domain.AuthDomain.UserID
import shop.domain.CartDomain.*
import shop.domain.CartDomain.Quantity
import shop.domain.ItemDomain.ItemID
import shop.domain.OrderDomain.Order
import shop.domain.OrderDomain.OrderID
import shop.domain.OrderDomain.PaymentID
import shop.domain.PaymentDomain.Payment
import shop.Generators.*
import shop.http.clients.PaymentClient
import shop.programs
import shop.services.OrderService
import shop.services.PaymentsService
import shop.services.ShoppingCartsService
import squants.market.Money
import weaver.scalacheck.Checkers
import weaver.SimpleIOSuite
import org.typelevel.log4cats.noop.NoOpLogger
import org.typelevel.log4cats.SelfAwareStructuredLogger
import shop.effects.TestBackground
import shop.effects.Background



object CheckoutSuite extends SimpleIOSuite with Checkers {

  val maxRetries = 3
  val retryPolicy:RetryPolicy[IO]=limitRetries[IO](maxRetries)

  def successfulClient(paymentID:PaymentID):PaymentsService[IO]=new PaymentsService[IO]{
    def process(payment: Payment): IO[PaymentID] = IO.pure(paymentID)
  }
  def successfulCart(cartTotal:CartTotal):ShoppingCartsService[IO]=
    new TestCart{
      override def get(userID:UserID):IO[CartTotal]=IO.pure(cartTotal)
      override def delete(userID:UserID):IO[Unit]=IO.unit
    }
  def successfulOrders(oid:OrderID):OrderService[IO]=new TestOrders{
    override def create(
      userId:UserID,
      paymentId:PaymentID,
      items:NonEmptyList[CartItem],
      total:Money
    ):IO[OrderID]=IO.pure(oid)
  }

  val gen=for{
    uid<-userIdGen
    pid<-paymentIdGen
    oid<-orderIdGen
    crt <- cartTotalGen
    crd <- cardGen

  } yield (uid, pid, oid, crt, crd)

  given bg:Background[IO]= TestBackground.noOp
  implicit val lg:SelfAwareStructuredLogger[IO] = NoOpLogger[IO]

  test("successful checkout"){
    forall(gen){
      case (uid,pid,oid,ct,card) =>
        Checkout[IO](successfulClient(pid), successfulCart(ct), successfulOrders(oid), retryPolicy)
          .process(uid,card)
          .map(expect.same(oid,_))      
    }  
  }
}
import shop.effects.Background

protected class TestCart() extends ShoppingCartsService[IO] {
  def add(userId: UserID, itemId: ItemID, quantity: Quantity): IO[Unit] = ???
  def get(userId: UserID): IO[CartTotal]                                = ???
  def delete(userId: UserID): IO[Unit]                                  = ???
  def removeItem(userId: UserID, itemId: ItemID): IO[Unit]              = ???
  def update(userId: UserID, cart: Cart): IO[Unit]                      = ???
}

protected class TestOrders() extends OrderService[IO] {
  def get(userId: UserID, orderId: OrderID): IO[Option[Order]]                                               = ???
  def findBy(userId: UserID): IO[List[Order]]                                                                = ???
  def create(userId: UserID, paymentId: PaymentID, items: NonEmptyList[CartItem], total: Money): IO[OrderID] = ???
}
