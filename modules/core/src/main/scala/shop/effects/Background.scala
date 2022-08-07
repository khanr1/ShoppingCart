package shop.effects

import scala.concurrent.duration.FiniteDuration
import cats.effect._
import cats.effect.std._
import cats.syntax.all.*

trait Background[F[_]] {
  def schedule[A](fa:F[A],duration:FiniteDuration):F[Unit]
}

object Background{
    def apply[F[_]:Background]:Background[F]=summon

    given bgInstance[F[_]](using T:Temporal[F],S:Supervisor[F]):Background[F]= new Background[F]{
        def schedule[A](fa:F[A],duration:FiniteDuration):F[Unit]=
            S.supervise(T.sleep(duration)*>fa).void
    }

}