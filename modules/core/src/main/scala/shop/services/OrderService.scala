package shop.services

import cats.data.NonEmptyList
import cats.effect.kernel.*
import cats.syntax.all.*
import shop.domain.AuthDomain.UserID
import shop.domain.CartDomain.CartItem
import shop.domain.CartDomain.Quantity
import shop.domain.ItemDomain.ItemID
import shop.domain.OrderDomain.Order
import shop.domain.OrderDomain.OrderID
import shop.domain.OrderDomain.PaymentID
import shop.sql.Codecs.*
import skunk.*
import skunk.circe.codec.all._
import skunk.implicits.*
import squants.market.Money
import shop.effects.GenUUID
import cats.effect.kernel.Resource
import shop.domain.ID

trait OrderService[F[_]]{
  def get(userid:UserID,orderid:OrderID):F[Option[Order]]
  def findBy(userID:UserID):F[List[Order]]
  def create(userid:UserID,paymentid:PaymentID,items:NonEmptyList[CartItem],total:Money):F[OrderID]

}

object OrderService{
  def make[F[_]:GenUUID:Concurrent](postgres : Resource[F,Session[F]]):OrderService[F]=
    new OrderService[F]{
      import OrderSQL.* 
      def get(userid: UserID, orderid: OrderID): F[Option[Order]] = postgres.use(s => s.prepare(selectByUserIDAndOrderID).use{
        pc => pc.option(userid ~ orderid)
      })
      def findBy(userID: UserID): F[List[Order]] = postgres.use{ s=> s.prepare(selectByUserID).use{
        pc=>pc.stream(userID, 1024).compile.toList
      }}
      def create(userid: UserID, paymentid: PaymentID, items: NonEmptyList[CartItem], total: Money): F[OrderID] = postgres.use{
        s => s.prepare(inserOrder).use{
          pc => ID.make[F,OrderID].flatMap(id=>
            val itMap=items.toList.map(x=> x.item.uuid -> x.quantity).toMap
            val order= Order(id,paymentid,itMap,total)
            pc.execute(userid,order).as(id)
          )
        }
      }
    }
}


object OrderSQL{
  val decoder:Decoder[Order] =
    (orderID ~ userID ~ paymentID ~ jsonb[Map[ItemID,Quantity]] ~ money).map{
      case o ~  _ ~ p ~ i ~ t => Order(o,p,i,t)
    }
  
  val encoder:Encoder[UserID ~ Order]=
    (orderID ~ userID ~ paymentID ~jsonb[Map[ItemID,Quantity]] ~ money).contramap{
      case id ~ o => o.id ~id ~ o.pid ~ o.items ~o.total
    }
  


  val selectByUserIDAndOrderID:Query[UserID ~ OrderID,Order]=
    sql""" SELECT * FROM orders WHERE user_id=$userID AND uuid=$orderID""".query(decoder)
  
  val selectByUserID:Query[UserID,Order] = 
    sql""" SELECT * FROM orders WHERE user_id=$userID""".query(decoder)
  
  val inserOrder:Command[UserID~Order]= 
    sql""" INSERT INTO orders VALUES ($encoder)""".command 
  
}
