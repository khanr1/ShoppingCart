package shop.services

import cats.effect._
import cats.syntax.all.*
import shop.domain.BrandDomain.*
import shop.domain.ItemDomain.*
import shop.domain.ID
import shop.sql.Codecs.*
import skunk.*
import skunk.implicits.*
import skunk.syntax.*
import shop.domain.CategoryDomain.Category
import shop.effects.GenUUID

trait ItemsService[F[_]] {
    def findAll:F[List[Item]]
    def findBy(brand:BrandName):F[List[Item]]
    def findById(itemid:ItemID):F[Option[Item]]
    def create(item:CreateItem):F[ItemID]
    def update(item:UpdateItem):F[Unit]
}

object ItemsService{
    def make[F[_]:Concurrent:GenUUID](postgres:Resource[F,Session[F]]):ItemsService[F]= new ItemsService[F]{
        import ItemSQL.*
        def findAll:F[List[Item]]=postgres.use(s=> s.execute(selectAll))
        def findBy(brand: BrandName): F[List[Item]] = postgres.use(s=> s.prepare(selectByBrand).use{ ps => ps.stream(brand,1024).compile.toList})
        def findById(itemid: ItemID): F[Option[Item]] = postgres.use(s=> s.prepare(selectById).use{ps => ps.option(itemid)})
        def create(item: CreateItem): F[ItemID] = postgres.use{s=>
            s.prepare(insertItem).use{pc=>
                ID.make[F,ItemID].flatMap{id=> pc.execute(id ~ item).as(id) }}}
        def update(item: UpdateItem): F[Unit] = postgres.use{
            session=> session.prepare(updateItem).use{ pc=> pc.execute(item).void}
        }
            
        
    }
}

private object ItemSQL{
    
    val decoder:Decoder[Item]= (itemID ~ itemName ~ itemDesc ~ money ~ brandID ~ brandName ~ categoryID ~ categoryName).map{
            case i ~ n ~ d ~ m  ~ bi ~ bn ~ci ~ cn => Item(i,n,d,m,Brand(bi,bn),Category(ci,cn))
        }

    val selectAll:Query[Void,Item] = 
        sql""""
            SELECT i.uuid, i.name, i.description, i.price, 
            b.uuid, b.name, c.uuid, c.name FROM items AS i
            INNER JOIN brands AS b ON i.brand_id = b.uuid
            INNER JOIN categories AS c ON i.category_id = c.uuid
        """.query(decoder)
    
    
    val selectByBrand:Query[BrandName,Item]=
        sql""" 
            SELECT i.uuid, i.name, i.description, i.price, 
            b.uuid, b.name, c.uuid, c.name FROM items AS i
            INNER JOIN brands AS b ON i.brand_id = b.uuid
            INNER JOIN categories AS c ON i.category_id = c.uuid
            WERE b.name LIKE $brandName
        """.query(decoder)
    
    val selectById:Query[ItemID,Item]=
        sql"""
            SELECT i.uuid, i.name, i.description, i.price, 
            b.uuid, b.name, c.uuid, c.name FROM items AS i
            INNER JOIN brands AS b ON i.brand_id = b.uuid
            INNER JOIN categories AS c ON i.category_id = c.uuid
            WHERE i.uuid=$itemID
        """.query(decoder)


    val insertItem:Command[ItemID ~ CreateItem] = 
        sql""" 
            INSERT INTO items VALUE($itemID,$itemName,$itemDesc,$money,$brandID, $categoryID)
        """.command.contramap{
            case id ~ i => id ~ i.name ~ i.description ~ i.price ~ i.brandid ~ i.categoryid
        }
    
    val updateItem:Command[UpdateItem] =
        sql" UPDATE items SET price $money WHERE uuid=$itemID".command.contramap{
            case i => i.price ~ i.id
        }
    
    
}
