package suite

import cats.effect.IO
import cats.implicits._
import io.circe._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import weaver.scalacheck.Checkers
import weaver.{ Expectations, SimpleIOSuite }

trait HttpSuite extends SimpleIOSuite with Checkers{

    def expectHttpBodyAndStatus[A:Encoder](route:HttpRoutes[IO],req:Request[IO])(
        expectedBody:A,
        expectedStatus:org.http4s.Status
    ):IO[Expectations] = route.run(req).value.flatMap{
        case Some(resp) =>
            resp.asJson.map{
                json=> expect.same(resp.status,expectedStatus) |+| expect.same(json,expectedBody.asJson)
            }
        case None => IO.pure(failure("route not found"))
    }

    def expectHttpStatus(routes: HttpRoutes[IO], req: Request[IO])(expectedStatus: org.http4s.Status): IO[Expectations] =
    routes.run(req).value.map {
      case Some(resp) => expect.same(resp.status, expectedStatus)
      case None       => failure("route nout found")
    }
  
}
