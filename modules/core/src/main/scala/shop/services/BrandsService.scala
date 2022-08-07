package shop.services

import cats.effect._
import cats.syntax.all.*
import shop.domain.BrandDomain._
import shop.domain.ID
import shop.effects.GenUUID
import shop.sql.Codecs.*
import skunk.*
import skunk.implicits.*

trait BrandsService[F[_]] {
  def findall:F[List[Brand]]
  def create(name:BrandName):F[BrandID]
}

object BrandsService{
  def make[F[_]:GenUUID:MonadCancelThrow](postgres:Resource[F,Session[F]]):BrandsService[F]= 
    new BrandsService[F]{
      import BrandSQL.*
      def findall: F[List[Brand]] = postgres.use(_.execute(selectALL))
      def create(name: BrandName): F[BrandID] = postgres.use(session =>
        session.prepare(insertBrand).use(cmd=> ID.make[F,BrandID].flatMap{
          id => cmd.execute(Brand(id,name)).as(id)
        }))
    }
}

private object BrandSQL{
  val codec:Codec[Brand]= (brandID ~ brandName).imap{
    case i ~ n =>Brand(i,n)
  }(brand=> brand.id ~ brand.name)

  val selectALL:Query[Void,Brand]= 
    sql"SELECT * FROM brands".query(codec)
  val insertBrand:Command[Brand]=
    sql"INSER INTO brands VALUE ($codec)".command
    
}