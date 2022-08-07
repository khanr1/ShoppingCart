package shop.domain

import cats.Functor
import shop.effects.GenUUID
import shop.optics.IsUUID
import cats.syntax.all.*

object ID {
  def make[F[_]:Functor:GenUUID,A:IsUUID]:F[A]=GenUUID[F].make.map(IsUUID[A]._UUID.get)
  def read[F[_]:Functor:GenUUID,A:IsUUID](s:String):F[A]=GenUUID[F].read(s).map(IsUUID[A]._UUID.get)
}
