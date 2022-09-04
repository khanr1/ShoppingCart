import shop.config.AppEnvironment

import shop.config.AppEnvironment.*

import cats.data.*
import cats.*
import cats.effect.*

import cats.syntax.all.*
import cats.implicits.*
import cats.effect.unsafe.implicits.global
import com.khanr1.auth.Jwt.*
import dev.profunktor.redis4cats.{ Redis, RedisCommands }
import dev.profunktor.redis4cats.log4cats.*
import io.circe.*
import io.circe.syntax.*
import java.util.UUID
import org.typelevel.log4cats.*
import pdi.jwt.JwtAlgorithm
import pdi.jwt.JwtClaim
import scala.concurrent.duration._
import shop.auth.*
import shop.config.Types.*
import shop.domain.AuthDomain.*
import shop.domain.AuthDomain.UserID
import shop.domain.BrandDomain.*
import shop.domain.CartDomain.*
import shop.domain.CartDomain.Cart
import shop.domain.CategoryDomain.*
import shop.domain.CheckOutDomain.CardName
import shop.domain.HealthCheckDomain.*
import shop.domain.HealthCheckDomain.Status
import shop.domain.HealthCheckDomain.Status.given
import shop.domain.ID
import shop.domain.ItemDomain.*
import shop.domain.ItemDomain.*
import shop.domain.OrderDomain.OrderID
import shop.domain.OrderDomain.OrderID.apply
import shop.domain.OrderDomain.PaymentID
import shop.http.auth.UserAuth.UserJwtAuth
import shop.http.auth.UserAuth.UserWithPassword
import shop.services.AuthsService
import shop.services.ItemsService
import shop.services.ShoppingCartsService
import shop.services.UsersAuth
import shop.services.UsersService
import squants.market.USD
import ciris.*

import shop.config.AppEnvironment.given
import shop.config.AppEnvironment
import shop.config.Config.*
import shop.config.Types.*

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

val Exp = ShoppingCartExpiration(30.seconds)
val tokenConfig = JwtAccessTokenKeyConfig("bar")
val tokenExp = TokenExpiration(30.seconds)
val jwtClaim = JwtClaim("test".asJson.noSpaces)
val userJwtAuth= UserJwtAuth(JwtAuth.hmac("bar",JwtAlgorithm.HS256))

val claim = JwtClaim(s"{\"id\":\"${UUID.randomUUID()}\"}")
val jwtAuth = JwtAuth.hmac("53cr3t", JwtAlgorithm.HS256)
val token = jwtEncode[IO](claim,jwtAuth.secret,jwtAuth.algo.head)
val decode = token.flatMap(x =>jwtDecode[IO](x,jwtAuth))

decode.unsafeRunSync()

UserID(UUID.randomUUID()).asJson.noSpaces

item.asJson.noSpaces


Status.show
Status.Okay.show
AppEnvironment.Test.show

// "test" match 
//     case "tsté"=> false
//     case "test"=> true
//     case _ => false
11+6
env("SC_APP_ENV").as[AppEnvironment].attempt[IO].unsafeRunSync()
env("SC_JWT_SECRET_KEY").as[String].map(JwtSecretKeyConfig(_)).secret.attempt[IO].unsafeRunSync()
