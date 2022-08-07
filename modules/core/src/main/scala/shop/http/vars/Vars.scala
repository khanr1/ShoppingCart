package shop.http.vars

import java.util.UUID
import cats.implicits.*
import shop.domain.ItemDomain.ItemID
import shop.domain.OrderDomain.OrderID
object Vars {
  protected class UUIDVar[A](f:UUID=>A){
      def unapply(str:String):Option[A]=
          Either.catchNonFatal(f(UUID.fromString(str))).toOption
  }
  object ItemIDVar extends UUIDVar(ItemID.apply)
  object OrderIDVar extends UUIDVar(OrderID.apply)
}

