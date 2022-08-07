package shop.retries

import cats.Show

enum Retriable {
  given show:Show[Retriable]=Show.fromToString
  case Orders extends Retriable
  case Payments extends Retriable
}
