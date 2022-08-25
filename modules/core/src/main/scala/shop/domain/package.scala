package shop

import io.circe.Decoder
import squants.market.Money
import squants.market.USD
import io.circe.Encoder
import cats.kernel.Monoid
import cats.Show
import cats.kernel.Eq

package object domain extends OrphanInstances

trait OrphanInstances{
    given decoder:Decoder[Money]=Decoder.decodeBigDecimal.map(USD.apply)
    given encoder:Encoder[Money]= Encoder.encodeBigDecimal.contramap(_.amount)
    given show:Show[Money]= Show.fromToString
    given eq:Eq[Money]=Eq.fromUniversalEquals
    given moneyMonoid:Monoid[Money]=
        new Monoid[Money]{
            def empty: Money=USD(0)
            def combine(x:Money,y:Money):Money=x+y
        }
}

