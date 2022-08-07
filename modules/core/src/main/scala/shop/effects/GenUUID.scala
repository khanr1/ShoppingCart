package shop.effects

import java.util.UUID
import cats.effect.kernel.Sync
import cats.ApplicativeThrow

trait GenUUID[F[_]] {
  def make:F[UUID]
  def read(s:String):F[UUID]
}

object GenUUID{
    def apply[F[_]](using GenUUID[F]):GenUUID[F]=summon[GenUUID[F]]
    given forSync[F[_]:Sync]:GenUUID[F]=new GenUUID[F]{
        def make: F[UUID] = Sync[F].delay(UUID.randomUUID())
        def read(s: String): F[UUID] = ApplicativeThrow[F].catchNonFatal(UUID.fromString(s))
    }
}