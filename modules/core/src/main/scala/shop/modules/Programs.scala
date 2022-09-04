package shop.modules

import shop.effects.Background
import org.typelevel.log4cats.Logger
import cats.effect.*
import shop.config.Types.*
import retry.RetryPolicy
import retry.RetryPolicies.*
import cats.syntax.all.*
import shop.programs.Checkout

sealed abstract class Programs[F[_]:Background:Logger:Temporal] private(
    cfg:CheckOutConfig,
    services:Services[F],
    clients:HttpClients[F]
){
    val retryPolicy:RetryPolicy[F] = 
        limitRetries[F](cfg.retriesLimit) |+| exponentialBackoff(cfg.retriesBackoff)

    val checkout:Checkout[F]=Checkout[F](
        clients.payment,
        services.cart,
        services.orders,
        retryPolicy
    )
        
}

object Programs {
  def make[F[_]: Background: Logger: Temporal](
      checkoutConfig: CheckOutConfig,
      services: Services[F],
      clients: HttpClients[F]
  ): Programs[F] =
    new Programs[F](checkoutConfig, services, clients) {}
}
