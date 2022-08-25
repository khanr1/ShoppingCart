package shop.http.clients



import cats.effect.MonadCancelThrow
import cats.syntax.all._
import org.http4s.Method._
import org.http4s._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe._
import org.http4s.client._
import org.http4s.client.dsl.Http4sClientDsl
import shop.domain.PaymentDomain.*
import shop.domain.OrderDomain.*
import shop.config.Types.PaymentConfig

trait PaymentClient[F[_]]{
    def process(payment:Payment):F[PaymentID]
}

object PaymentClient{
    def make[F[_]:MonadCancelThrow:JsonDecoder](cfg:PaymentConfig,client:Client[F]):PaymentClient[F]={
        new PaymentClient[F] with Http4sClientDsl[F]{
            

            def process(payment: Payment): F[PaymentID] = 
                Uri.fromString(cfg.uri.value+ "/payments")
                    .liftTo[F]
                    .flatMap{ uri =>
                        client.run(POST(payment,uri)).use{ resp =>
                            resp.status match{ 
                                case Status.Ok | Status.Conflict => resp.asJsonDecode[PaymentID]
                                case st => OrderOrPaymentError.PaymentError(Option(st.reason).getOrElse("unknown")).raiseError[F,PaymentID]
                            }}
                    }
        }
    }
}
