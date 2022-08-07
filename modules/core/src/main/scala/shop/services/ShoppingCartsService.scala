package shop.services

import shop.domain.ItemDomain.*
import shop.domain.AuthDomain.*
import shop.domain.CartDomain.*
import shop.config.Types.*
import shop.domain.given

import cats.MonadThrow
import cats.syntax.all.*
import shop.effects.GenUUID
import dev.profunktor.redis4cats.RedisCommands
import shop.domain.ID




trait ShoppingCartsService[F[_]] {
    def add(
        userId:UserID,
        itemId:ItemID,
        quantity:Quantity
    ):F[Unit]
    def get(userId:UserID):F[CartTotal]
    def delete(userId:UserID):F[Unit]
    def removeItem(userId:UserID,itemId:ItemID):F[Unit]
    def update(userId:UserID,cart:Cart):F[Unit]
}

object ShoppingCartsService{
    def make[F[_]:GenUUID:MonadThrow](
        items:ItemsService[F],
        redis:RedisCommands[F,String,String],
        exp : ShoppingCartExpiration
    ):ShoppingCartsService[F]= new ShoppingCartsService[F]{
        def add(userId: UserID, itemId: ItemID, quantity: Quantity): F[Unit] =
            redis.hSet(userId.show,itemId.show,quantity.show) *> redis.expire(itemId.show,exp.value).void
        def get(userId: UserID): F[CartTotal] = redis.hGetAll(userId.show).flatMap{
            x => x.toList.traverseFilter{
                case (k,v) => for{
                    id <- ID.read[F,ItemID](k)
                    qt <- MonadThrow[F].catchNonFatal(Quantity(v.toInt))
                    rs <- items.findById(id).map(someItem => someItem.map(_.cart(qt)))
                }yield rs
            }
            .map{items=>
                CartTotal(items,items.foldMap(_.subtotal))    
            }
        }
        def delete(userId: UserID): F[Unit] = redis.del(userId.show).void
        def removeItem(userId: UserID, itemId: ItemID): F[Unit] = redis.hDel(userId.show,itemId.show).void
        def update(userId: UserID, cart: Cart): F[Unit] = redis.hGetAll(userId.show).flatMap{
            m => m.toList.traverse_{
                case (itemId_F,q) => ID.read[F,ItemID](itemId_F).flatMap{
                    id=> cart.items.get(id).traverse_{
                        q => redis.hSet(userId.show,itemId_F,q.show)
                    }
                }  
            } *> redis.expire(userId.show,exp.value).void
        }
    }
}