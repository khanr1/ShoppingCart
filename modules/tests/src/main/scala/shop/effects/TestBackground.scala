package shop.effects

package shop.effects

import cats.effect.IO
import scala.concurrent.duration.FiniteDuration

object TestBackground {
    val noOp:Background[IO]= new Background[IO]{
        def schedule[A](fa: IO[A], duration: FiniteDuration): IO[Unit] = IO.unit
    }
  
}
