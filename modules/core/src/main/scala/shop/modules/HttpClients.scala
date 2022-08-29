package shop.modules

import shop.http.clients.PaymentClient
import shop.config.Types.PaymentConfig
import org.http4s.client.Client
import cats.effect.*
import org.http4s.circe.JsonDecoder

trait HttpClients[F[_]] {
  def payment:PaymentClient[F]
}

object HttpClients{
    def make[F[_]:MonadCancelThrow:JsonDecoder](
        cfg:PaymentConfig,
        client:Client[F]
    ):HttpClients[F] = new HttpClients{
        override def payment: PaymentClient[F] = PaymentClient.make[F](cfg,client)
    }
}