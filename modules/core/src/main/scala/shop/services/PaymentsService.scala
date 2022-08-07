package shop.services

import shop.domain.PaymentDomain.Payment
import shop.domain.OrderDomain.PaymentID

trait PaymentsService[F[_]] {
  def process(payment:Payment):F[PaymentID]
}
