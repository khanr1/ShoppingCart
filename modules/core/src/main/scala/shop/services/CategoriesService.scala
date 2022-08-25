package shop.services

import cats.effect.*
import cats.syntax.all.*
import shop.domain.CategoryDomain.*
import shop.domain.ID
import shop.effects.GenUUID
import shop.sql.Codecs.*
import skunk.*
import skunk.implicits.*




trait CategoriesService[F[_]] {
    def findall:F[List[Category]]
    def create(name:CategoryName):F[CategoryID]
}

object CategoriesService{
    def make[F[_]:GenUUID:MonadCancelThrow](postgres:Resource[F,Session[F]]):CategoriesService[F]=
        new CategoriesService[F]{
            import CategorySQL.*
            def findall: F[List[Category]] = postgres.use(s => s.execute(selectAll))
            def create(name: CategoryName): F[CategoryID] = postgres.use{ session => 
                session.prepare(insertCategory).use{cmd=> ID.make[F,CategoryID].flatMap{
                    id => cmd.execute(Category(id,name)).as(id)
                }}
            }
                
        }
}

private object CategorySQL{
    val codec:Codec[Category]=(categoryID ~ categoryName).imap{case i ~ n => Category(i,n) }(cat => cat.id ~ cat.name)
    val selectAll: Query[Void, Category] =
    sql"""
        SELECT * FROM categories
       """.query(codec)
    val insertCategory: Command[Category] =
        sql"""
            INSERT INTO categories
            VALUES ($codec)
            """.command

}