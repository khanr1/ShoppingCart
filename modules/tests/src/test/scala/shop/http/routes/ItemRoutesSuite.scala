package shop.http.routes

import suite.HttpSuite
import shop.services.ItemsService
import shop.domain.ItemDomain.CreateItem
import shop.domain.ItemDomain.ItemID
import cats.effect.IO
import shop.domain.ItemDomain.Item
import shop.domain.BrandDomain.BrandName
import shop.domain.ItemDomain.UpdateItem
import org.scalacheck.Gen
import shop.Generators.*
import org.http4s.client.dsl.io.*
import org.http4s.Method.*
import org.http4s.*
import org.http4s.syntax.literals.*


 
object ItemRoutesSuite extends HttpSuite{

    def dataItems(items:List[Item])=new TestItems{
        override  def findAll: IO[List[Item]] = IO.pure(items)
        override  def findBy(brand: BrandName): IO[List[Item]] = IO.pure(
            items.find(_.brand.name==brand).toList
        )
    }

    test("Get items bu brand sucess"){
        val gen=for{
            i<-Gen.listOf(itemGen)
            b<-brandGen
        } yield i->b
        forall(gen){
            case (it,b)=> 
                val req =GET(uri"/items".withQueryParam("brand",b.name.value))
                val routes = new ItemRoutes[IO](dataItems(it)).routes
                val expected = it.find(_.brand.name==b.name).toList
                expectHttpBodyAndStatus(routes,req)(expected,Status.Ok)

        }
    }

  
}

protected class TestItems extends ItemsService[IO]{
    override def create(item: CreateItem): IO[ItemID] = ???
    override def findAll: IO[List[Item]] = ???
    override def findBy(brand: BrandName): IO[List[Item]] = ???
    override def findById(itemid: ItemID): IO[Option[Item]] = ???
    override def update(item: UpdateItem): IO[Unit] = ???
}