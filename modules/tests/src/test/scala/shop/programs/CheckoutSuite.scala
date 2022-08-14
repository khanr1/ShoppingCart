package shop.programs

import cats.data.NonEmptyList
import cats.effect.IO
import cats.syntax.all.*
import org.typelevel.log4cats.noop.NoOpLogger
import org.typelevel.log4cats.SelfAwareStructuredLogger
import retry.RetryPolicies.*
import retry.RetryPolicy
import shop.domain.AuthDomain.UserID
import shop.domain.CartDomain.*
import shop.domain.CartDomain.Quantity
import shop.domain.ItemDomain.ItemID
import shop.domain.OrderDomain.EmptyCartError
import shop.domain.OrderDomain.Order
import shop.domain.OrderDomain.OrderID
import shop.domain.OrderDomain.OrderOrPaymentError
import shop.domain.OrderDomain.PaymentID
import shop.domain.PaymentDomain.Payment
import shop.effects.Background
import shop.effects.TestBackground
import shop.Generators.*
import shop.http.clients.PaymentClient
import shop.programs
import shop.retries.TestRetry.*
import shop.services.*
import squants.market.*
import weaver.scalacheck.Checkers
import weaver.*
import retry.RetryDetails.GivingUp
import retry.RetryDetails.WillDelayAndRetry
import cats.effect.kernel.Ref
import shop.retries.*
import scala.concurrent.duration.*




object CheckoutSuite extends SimpleIOSuite with Checkers {

  val maxRetries = 3
  val retryPolicy:RetryPolicy[IO]=limitRetries[IO](maxRetries)

  def successfulClient(paymentID:PaymentID):PaymentClient[IO]=new PaymentClient[IO]{
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

  def recoveringClient(
    attemptsSoFar:Ref[IO,Int],
    paymentID:PaymentID
  ): PaymentClient[IO]=
    new PaymentClient[IO]{
      def process(payment: Payment): IO[PaymentID] = attemptsSoFar.get.flatMap{
        case n if n == 1 => IO.pure(paymentID)
        case _ => attemptsSoFar.update(_ +1) *> IO.raiseError(OrderOrPaymentError.PaymentError(""))
      }
    }


  val emptyCart:ShoppingCartsService[IO]=new TestCart{
    override def get(userId: UserID): IO[CartTotal] = IO.pure(CartTotal(List.empty,USD(0)))
  }

  val unreachableClient: PaymentClient[IO] =
    new PaymentClient[IO]{
      def process(payment: Payment): IO[PaymentID] =
        IO.raiseError(OrderOrPaymentError.PaymentError(""))
    }

  val gen=for{
    uid<-userIdGen
    pid<-paymentIdGen
    oid<-orderIdGen
    crt <- cartTotalGen
    crd <- cardGen

  } yield (uid, pid, oid, crt, crd)

  val failingOrder:OrderService[IO]=new TestOrders{
    override def create
    (userId: UserID, 
     paymentId: PaymentID,
     items: NonEmptyList[CartItem],
     total: Money): IO[OrderID] =
       IO.raiseError(OrderOrPaymentError.OrderError(""))
  }

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

  test("empty cart"){
    forall(gen){
      case (uid,pid,oid,_,card) => 
        Checkout[IO](
          successfulClient(pid),
          emptyCart,
          successfulOrders(oid),
          retryPolicy
        ).process(uid,card)
         .attempt
         .map{
            case Left(EmptyCartError) => success
            case _ => failure("cart was not empty")
         }
    }
  }

  test("unreachable payment client"){
    forall(gen){
      case (uid,pid,oid,ct,card) =>
        Ref.of[IO,Option[GivingUp]](None).flatMap{retries =>
          given rh:Retry[IO]= TestRetry.givingUp(retries)
          Checkout[IO](unreachableClient,successfulCart(ct),successfulOrders(oid),retryPolicy)
            .process(uid,card)
            .attempt
            .flatMap{
              case Left(OrderOrPaymentError.PaymentError(_))=> retries.get.map{
                case Some(g) => expect.same(g.totalRetries,maxRetries)
                case None => failure("expected given up")
              }
              case _ => IO.pure(failure("Expected payment error"))
            }
        }
    }
  }

  test("failing payment client succeeds after one retry"){
    forall(gen){
      case(uid,pid,oid,ct,card)=> 
        (Ref.of[IO,Option[WillDelayAndRetry]](None),Ref.of[IO,Int](0)).tupled.flatMap{
          case (retries,cliRef) => 
            given  rh:Retry[IO]=TestRetry.recovering(retries)
            Checkout[IO](
              recoveringClient(cliRef,pid),
              successfulCart(ct),
              successfulOrders(oid),
              retryPolicy
            ).process(uid,card)
            .attempt
            .flatMap{
              case Right(id) => retries.get.map{
                case Some(w) => expect.same(id,oid) |+| expect.same(0,w.retriesSoFar)
                case None => failure("expected 1 try")
              }
              case Left(_) => IO.pure(failure("expected PaymentID"))
            }
        }
    }
  }

  test("cannot create order, run in the background"){
    forall(gen){
      case (uid,pid,_,ct,card) =>
        (Ref.of[IO,(Int,FiniteDuration)](0 -> 0.seconds),Ref.of[IO,Option[GivingUp]](None))
        .tupled
        .flatMap{
          case (acc,retries)=>
            given bg:Background[IO]=TestBackground.counter(acc)
            given rh:Retry[IO]=TestRetry.givingUp(retries)
            Checkout[IO](
              successfulClient(pid),
              successfulCart(ct),
              failingOrder,
              retryPolicy)
              .process(uid,card)
              .attempt
              .flatMap{
                case Left(OrderOrPaymentError.OrderError(_))=> 
                  (acc.get,retries.get).mapN{
                    case (c,Some(g)) => 
                      expect.same(c,1 -> 1.hour) |+| expect.same(g.totalRetries, maxRetries)
                    case _ => failure(s"Expected $maxRetries retries and schedule ")
                  }
                case _ => IO.pure(failure("Expected order error"))
                
              }
            
        }
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
