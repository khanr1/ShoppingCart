import shop.domain.CheckOutDomain.CardName
import shop.domain.BrandDomain.BrandParam
import shop.domain.HealthCheckDomain.Status
import shop.domain.HealthCheckDomain.*
import shop.domain.ItemDomain.*
import shop.domain.CartDomain.*
import shop.domain.AuthDomain.*
import io.circe.syntax.*
import cats.syntax.all.*

import java.util.UUID

val id=ItemID(UUID.randomUUID())
val q= Quantity(8)
q.show
val m=Map((id,q))

val cart=Cart.apply(m)
cart.asJson
cart.asJson.as[Cart]


val Rs=RedisStatus(Status.Okay)
val Ps=PostgresStatus(Status.Okay)

Rs.asJson

AppStatus(Rs,Ps).asJson

BrandParam("test")

CardName("$tat").leftMap(_.toNonEmptyList)


val mapTest =Map(("k"->5), ("c"-> 3))

mapTest.toList