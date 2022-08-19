package shop.http.routes.client

import weaver.* 
import weaver.scalacheck.Checkers
import cats.effect.IO
import org.http4s.*
import shop.Generators.*
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.implicits.*
import org.http4s.*
import shop.http.clients.PaymentClient
import shop.domain.OrderDomain.OrderOrPaymentError.PaymentError
import shop.config.Types.PaymentConfig
import shop.config.Types.PaymentURI

object PaymentClientSuite extends SimpleIOSuite with Checkers{
    val config = PaymentConfig(PaymentURI("http://localhost"))

    def routes(mkResponse:IO[Response[IO]])= HttpRoutes.of[IO] {
        case POST -> Root /"payments" => mkResponse
    }.orNotFound 

    val gen = for{
        id<-paymentIdGen
        p <-paymentGen
    } yield id -> p

    test("Response Ok (200) "){
        forall(gen){
            case (pid,payment) => 
                val client = Client.fromHttpApp(routes(Status.Ok(pid)))
                PaymentClient.make[IO](config,client)
                    .process(payment)
                    .map(id=> expect.same(pid.value,id.value))
        }
    }

    test("Response Conflict (409)") {
      forall(gen) {
      case (pid, payment) =>
        val client = Client.fromHttpApp(routes(Conflict(pid)))

        PaymentClient
          .make[IO](config,client)
          .process(payment)
          .map(expect.same(pid, _))
      }
    }
    test("Internal Server Error response (500)") {
      forall(gen) {
        case (pid, payment) =>
          val client = Client.fromHttpApp(routes(InternalServerError()))

          PaymentClient
            .make[IO](config,client)
            .process(payment)
            .attempt
            .map{
              case Left(e) =>expect.same(PaymentError("Internal Server Error"),e)
              case Right(_) => failure("expected payment error")
            }
      }
    }

}
