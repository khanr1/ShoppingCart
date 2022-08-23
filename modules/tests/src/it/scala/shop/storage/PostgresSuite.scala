package shop.storage

import cats.data.*
import cats.effect.*
import cats.implicits.*
import natchez.Trace.Implicits.noop
import scala.languageFeature.postfixOps
import shop.domain.BrandDomain.*
import shop.domain.CategoryDomain.*
import shop.domain.ItemDomain.*
import shop.Generators.*
import shop.services.BrandsService
import shop.services.CategoriesService
import shop.services.ItemsService
import shop.services.UsersService
import skunk.*
import skunk.implicits.*
import suite.ResourceSuite
import org.scalacheck.Gen
import shop.services.OrderService
import shop.domain.given

object PostgresSuite extends ResourceSuite{
  type Res= Resource[IO,Session[IO]]

  val flushTables:List[Command[Void]]=
    List("items","brands","categories","orders","users").map{ table=> sql"DELETE FROM #$table".command}

  override def sharedResource: Resource[IO,Res] = 
    Session.pooled[IO](
      host = "localhost",
      port = 5432,
      user = "bot",
      password =Some("banana"),
      database = "store",
      max = 10
    ).beforeAll{ 
      _.use{ s => flushTables.traverse_(s.execute)
      }
    }

  test("Brands") { postgres =>
    forall(brandGen) { brand =>
      val b = BrandsService.make[IO](postgres)
      for {
        x <- b.findall
        _ <- b.create(brand.name)
        y <- b.findall
        z <- b.create(brand.name).attempt
      } yield expect.all(x.isEmpty, y.count(_.name === brand.name) === 1, z.isLeft)
    }
  }


  test("Categories") { postgres =>
    forall(categoryGen) { category =>
      val b = CategoriesService.make[IO](postgres)
      for {
        x <- b.findall
        _ <- b.create(category.name)
        y <- b.findall
        z <- b.create(category.name).attempt
      } yield expect.all(x.isEmpty, y.count(_.name === category.name) === 1, z.isLeft)
    }
  }

  test("Items"){postgres=>
    forall(itemGen){ item =>
        def newItem(
          bid:Option[BrandID],
          cid:Option[CategoryID]
        )= CreateItem(
          item.name,
          item.description,
          item.price,
          bid.getOrElse(item.brand.id),
          cid.getOrElse(item.category.id)
        )
        val b= BrandsService.make[IO](postgres)
        val c= CategoriesService.make[IO](postgres)
        val i= ItemsService.make[IO](postgres)

        for{
          x <-i.findAll
          _ <-b.create(item.brand.name)
          d <-b.findall.map(_.headOption.map(_.id))
          _ <-c.create(item.category.name)
          e <-c.findall.map(_.headOption.map(_.id))
          _ <-i.create(newItem(d,e))
          y <-i.findAll
        } yield expect.all(x.isEmpty,y.count(_.name===item.name)===1)
      }  
  }

  test("User"){postgres => 
    val gen = for{
      u <- userNameGen
      p <- encryptedPasswordGen
    }  yield u -> p
    forall(gen){
      case (username,password) =>
        val u=UsersService.make[IO](postgres)
        for{
          d<- u.create(username,password)
          x<- u.find(username)
          z<- u.create(username,password).attempt
        } yield expect.all(x.count(_.id===d)===1,z.isLeft)
    }
  }
  test("Orders") { postgres =>
    val gen = for {
      oid <- orderIdGen
      pid <- paymentIdGen
      un  <- userNameGen
      pw  <- encryptedPasswordGen
      it  <- Gen.nonEmptyListOf(cartItemGen).map(NonEmptyList.fromListUnsafe)
      pr  <- moneyGen
    } yield (oid, pid, un, pw, it, pr)

    forall(gen) {
      case (oid, pid, un, pw, items, price) =>
        val o = OrderService.make[IO](postgres)
        val u = UsersService.make[IO](postgres)
        for {
          d <- u.create(un, pw)
          x <- o.findBy(d)
          y <- o.get(d, oid)
          i <- o.create(d, pid, items, price)
        } yield expect.all(x.isEmpty, y.isEmpty, i.value.version === 4)
    }
  }
}