package shop.programs

import shop.services.*
import shop.retries.Retry
import shop.retries.Retriable

import shop.domain.OrderDomain.*
import shop.domain.PaymentDomain.*
import shop.domain.AuthDomain.*
import shop.domain.CartDomain.*
import shop.domain.ItemDomain.*
import shop.domain.CheckOutDomain.*
import shop.effects.Background

import cats.data.NonEmptyList
import cats.MonadThrow
import cats.syntax.all.*
import retry.*
import org.typelevel.log4cats.Logger
import squants.market.Money
import concurrent.duration._
import shop.http.clients.PaymentClient

final case class Checkout[F[_]:Logger:Background:MonadThrow:Retry](
    payments:PaymentClient[F],
    cart: ShoppingCartsService[F],
    orders:OrderService[F],
    policy:RetryPolicy[F]
){
    private def ensureNonEmpty[A](xs:List[A]):F[NonEmptyList[A]]={
        MonadThrow[F].fromOption(
            NonEmptyList.fromList(xs),
            EmptyCartError
        )
    }

    def processPayment(in:Payment):F[PaymentID]= 
        Retry[F]
        .retry(policy,Retriable.Payments)(payments.process(in))
        .adaptError{
            case e => OrderOrPaymentError.PaymentError(Option(e.getMessage).getOrElse("Unknown Error"))
        }
    

    def createOrder(userId:UserID,paymentId:PaymentID,items:NonEmptyList[CartItem],total:Money):F[OrderID]={
        val action:F[OrderID] = Retry[F]
        .retry(policy,Retriable.Orders)(orders.create(userId,paymentId,items,total))
        .adaptError{
            case e=> OrderOrPaymentError.OrderError(e.getMessage)
        }
        def bgAction(fa:F[OrderID]):F[OrderID]= fa.onError{
            case _ => Logger[F].error(
                            s"Failed to create order for Payment: ${paymentId.show}. Rescheduling as a background action"
            )*>Background[F].schedule(bgAction(fa),1.hour)
        }

        bgAction(action)

    }
    def process(userid:UserID,card:Card):F[OrderID]=for{
        c <- cart.get(userid)
        its <- ensureNonEmpty(c.items)
        pid<- processPayment(Payment(userid,c.total,card))
        oid<- createOrder(userid,pid,its,c.total)
        _ <- cart.delete(userid).attempt.void
    } yield oid
}
