package shop.programs

import retry.RetryPolicy
import cats.effect.IO
import retry.RetryPolicies.*
import shop.domain.OrderDomain.PaymentID
import shop.http.clients.PaymentClient
import shop.domain.PaymentDomain.Payment
import shop.domain.OrderDomain.OrderID

object CheckoutSuite {
  val maxRetries = 3
  val retryPolicy:RetryPolicy[IO]=limitRetries[IO](maxRetries)

  def successfulClient(paymentID:PaymentID):PaymentClient[IO]=new PaymentClient[IO]{
    def process(payment: Payment): IO[PaymentID] = IO.pure(paymentID)
  }
}
