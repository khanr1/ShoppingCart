package shop.http.routes

import shop.Generators.*
import shop.services.BrandsService

import cats.effect.*
import org.http4s.*
import org.http4s.client.dsl.io.*
import org.http4s.Method.*
import org.http4s.syntax.literals.*
import org.scalacheck.Gen
import shop.domain.BrandDomain.*

import suite.HttpSuite


object BrandRoutesSuite extends HttpSuite{

    def dataBrands(brands: List[Brand])= new TestBrands{
        override def findall: IO[List[Brand]] = IO.pure(brands)
    }

    test("Get brands succeeds"){
        forall(Gen.listOf(brandGen)){ b=>
            val req =GET(uri"/brands")
            val routes= new BrandRoutes[IO](dataBrands(b)).routes
            expectHttpBodyAndStatus(routes, req)(b, Status.Ok)
        }
    }


    
  
}


protected class TestBrands extends BrandsService[IO] {
    override def findall: IO[List[Brand]] = ???
    override def create(name: BrandName): IO[BrandID] = ???
}
