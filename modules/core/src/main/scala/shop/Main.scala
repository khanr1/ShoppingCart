package shop

import cats.effect.IOApp
import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.effect.IO
import shop.config.Config
import org.typelevel.log4cats.Logger
import cats.effect.std.Supervisor

object Main extends IOApp.Simple{
    given logger:Logger[IO]= Slf4jLogger.getLogger[IO]

    override def run: IO[Unit] = IO.println("HelloWorld")
}
  
