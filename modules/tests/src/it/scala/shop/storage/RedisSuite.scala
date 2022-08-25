package shop.storage

import suite.ResourceSuite

import scala.concurrent.duration._

import io.circe.*
import io.circe.syntax.*
import cats.effect.*
import cats.implicits.*
import cats.syntax.all.*
import com.khanr1.auth.Jwt.*
import dev.profunktor.redis4cats.{ Redis, RedisCommands }
import dev.profunktor.redis4cats.log4cats.*
import org.typelevel.log4cats.*
import org.typelevel.log4cats.noop.NoOpLogger
import pdi.jwt.JwtAlgorithm
import pdi.jwt.JwtClaim
import shop.config.Types.*
import shop.domain.BrandDomain.*
import shop.domain.CategoryDomain.*
import shop.domain.ID
import shop.domain.ItemDomain.*
import shop.Generators.*
import shop.http.auth.UserAuth.UserJwtAuth
import shop.services.ItemsService
import shop.storage.RedisSuite.Res
import shop.services.ShoppingCartsService
import shop.domain.CartDomain.Cart
import shop.domain.AuthDomain.*
import shop.services.UsersService
import shop.domain.AuthDomain.EncryptedPassword
import shop.domain.AuthDomain.UserID
import shop.http.auth.UserAuth.UserWithPassword
import shop.auth.JwtExpire
import shop.auth.Crypto
import shop.services.AuthsService
import shop.services.UsersAuth
import java.util.UUID


object RedisSuite extends ResourceSuite {

    given logger:SelfAwareStructuredLogger[IO]=NoOpLogger[IO]
    type Res=RedisCommands[IO,String,String]

    override def sharedResource: Resource[cats.effect.IO, Res] = Redis[IO].utf8("redis://localhost").beforeAll(_.flushAll)


    val Exp = ShoppingCartExpiration(30.seconds)
    val tokenConfig = JwtAccessTokenKeyConfig("bar")
    val tokenExp = TokenExpiration(30.seconds)
    val jwtClaim = JwtClaim("test".asJson.noSpaces)
    val userJwtAuth= UserJwtAuth(JwtAuth.hmac("bar",JwtAlgorithm.HS256))

    test("Shopping Cart"){ redis =>
        val gen = for{
            uid <- userIdGen
            it1 <- itemGen
            it2 <- itemGen
            q1 <- quantityGen
            q2 <- quantityGen
        } yield (uid,it1,it2,q1,q2)

        forall(gen) {
            case (uid,it1,it2,q1,q2) =>
                Ref.of[IO,Map[ItemID,Item]](Map(it1.uuid->it1,it2.uuid->it2)).flatMap{ref=>
                    val items =new TestItemsService(ref)
                    val c     =ShoppingCartsService.make[IO](items,redis,Exp)
                    for{
                        x <- c.get(uid)
                        _ <- c.add(uid,it1.uuid,q1)
                        _ <- c.add(uid,it2.uuid,q2)
                        y <- c.get(uid)
                        _ <- c.removeItem(uid,it1.uuid)
                        z <- c.get(uid)
                        _ <- c.update(uid,Cart(Map(it2.uuid-> q2)))
                        w <- c.get(uid)
                        _ <- c.delete(uid)
                        v <- c.get(uid)

                    } yield expect.all(
                        x.items.isEmpty,
                        y.items.size===2,
                        z.items.size ===1,
                        v.items.isEmpty,
                        w.items.headOption.fold(false)(_.quantity===q2)
                    )
                }
        }
    }

    test("Authentication"){redis =>
        val gen  = for{
            un1 <- userNameGen
            un2 <- userNameGen
            pw  <- passwordGen
        } yield (un1,un2,pw)

        forall(gen){
            case (un1,un2,pw) =>
                for{
                    t <- JwtExpire.make[IO].map(shop.auth.Tokens.make[IO](_,tokenConfig,tokenExp))
                    c <- Crypto.make[IO](PasswordSalt("test".asJson.noSpaces))
                    a = AuthsService.make(tokenExp,t,new TestUsersService(un2),redis,c)
                    u = UsersAuth.common[IO](redis)
                    x <- u.findUser(com.khanr1.auth.Jwt.JwtToken.apply("invalid"))(jwtClaim)
                    y <- a.login(un1,pw).attempt // usernotfound
                    j <- a.newUser(un1,pw)
                    e <- jwtDecode[IO](j, userJwtAuth.value).attempt
                    k <- a.login(un2, pw).attempt // InvalidPassword
                    w <- u.findUser(j)(jwtClaim)
                    s <- redis.get(j.value)
                    _ <- a.logout(j, un1)
                    z <- redis.get(j.value)

                } yield expect.all(
                        x.isEmpty,
                        y == Left(UserNotFound(un1)),
                        e.isRight,
                        k == Left(InvalidPassword(un2)),
                        w.fold(false)(_.value.name === un1),
                        s.nonEmpty,
                        z.isEmpty
                    )
        }
    }




}

protected class TestUsersService(un:UserName) extends UsersService[IO]{
    override def create(username: UserName, password: EncryptedPassword): IO[UserID] = ID.make[IO,UserID]
    override def find(username: UserName): IO[Option[UserWithPassword]] = IO.pure{
        (username === un).guard[Option].as{
            UserWithPassword(UserID(UUID.randomUUID),un,EncryptedPassword("foo"))
        }

    }
}



protected class TestItemsService(ref: Ref[IO,Map[ItemID,Item]]) extends ItemsService[IO]{

    override def update(item: UpdateItem): IO[Unit] = ref.update{ x=> x.get(item.id).fold(x){ i => x.updated(item.id,i.copy(price = item.price))}}

    override def create(item: CreateItem): IO[ItemID] = ID.make[IO,ItemID].flatMap{ id =>
        val brand = Brand(item.brandid,BrandName("foo"))
        val category = Category(item.categoryid,CategoryName("foo"))
        val newItem = Item(id,item.name,item.description,item.price,brand,category)
        ref.update(_.updated(id,newItem))*> IO.pure(id)
    }

    override def findById(itemid: ItemID): IO[Option[Item]] = ref.get.map(_.get(itemid))

    override def findAll:IO[List[Item]] = ref.get.map(_.values.toList)

    override def findBy(brand:BrandName):IO[List[Item]]=ref.get.map(_.values.filter(_.brand.name === brand).toList)
}
