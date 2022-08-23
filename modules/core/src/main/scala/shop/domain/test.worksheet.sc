import cats.data.NonEmptyList
import shop.domain.BrandDomain.BrandID
import squants.market.USD
import shop.domain.OrderDomain.PaymentID
import shop.domain.OrderDomain.OrderID
import shop.domain.OrderDomain.OrderID.apply
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
val brand= shop.domain.BrandDomain.Brand.apply(BrandID(UUID.randomUUID()),shop.domain.BrandDomain.BrandName.apply("test"))
val cat= shop.domain.CategoryDomain.Category.apply(shop.domain.CategoryDomain.CategoryID.apply(UUID.randomUUID()),shop.domain.CategoryDomain.CategoryName.apply("test cat"))
val id=ItemID(UUID.randomUUID())
val itemname=ItemName("test item name")
val itemdescriptio=ItemDescription("test item description")

val item=Item(id,itemname,itemdescriptio,USD(5),brand,cat)
val cartitem=CartItem(item,q)
val orderid=OrderID(UUID.randomUUID())
val paymentid=PaymentID(UUID.randomUUID())
brand.show
itemname.show
paymentid.show
itemdescriptio.show
orderid.show
cartitem.show
val q= Quantity(8)
q.show
val m=Map((id,q))
NonEmptyList.fromListUnsafe(List(cartitem)).show
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