package shop.modules

import shop.http.clients.PaymentClient
import shop.config.Types.PaymentConfig
import org.http4s.client.Client
import cats.effect.MonadCancelThrow
import org.http4s.circe.JsonDecoder

trait HttpClients[F[_]] {
  def payment:PaymentClient[F]
}

object HttpClients{
    def make[F[_]: JsonDecoder: MonadCancelThrow](
        cfg:PaymentConfig,
        client:Client[F]
    ):HttpClients[F] = new HttpClients[F]{
        override def payment: PaymentClient[F] = PaymentClient.make[F](cfg,client)
    }
}