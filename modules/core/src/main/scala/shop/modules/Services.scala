package shop.modules

import shop.services.*
import shop.effects.GenUUID

import cats.effect.*
import dev.profunktor.redis4cats.RedisCommands
import skunk.Session
import shop.config.Types.*

sealed abstract class Services[F[_]] private (
    val cart:ShoppingCartsService[F],
    val brands : BrandsService[F],
    val categories :CategoriesService[F],
    val items : ItemsService[F],
    val orders: OrderService[F],
    val healthCheck: HealthCheckServices[F]
)

object Services{
    def make[F[_]:GenUUID:Temporal](
        redis:RedisCommands[F,String,String],
        postgres:Resource[F,Session[F]],
        cartExpiration: ShoppingCartExpiration
    ): Services[F] = {
        val _items= ItemsService.make[F](postgres)
        new Services[F](
            cart=ShoppingCartsService.make[F](_items,redis,cartExpiration),
            brands=BrandsService.make[F](postgres),
            categories =CategoriesService.make[F](postgres),
            items = _items,
            orders = OrderService.make[F](postgres),
            healthCheck = HealthCheckServices.make[F](postgres,redis)
        ){}
    }
}
