package shop.ext.http4s

import org.http4s.Request
import io.circe.Decoder
import cats.MonadThrow
import cats.syntax.all._
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._ 
import org.http4s.Response
import org.http4s._

trait  refined[F[_]:MonadThrow:JsonDecoder] extends Http4sDsl[F]{
      extension  (req:Request[F]){
        def decodeR[A:Decoder](f:A=>F[Response[F]]):F[Response[F]]=
            req.asJsonDecode[A].attempt.flatMap{
                case Left(e)  => UnprocessableEntity()
                case Right(a) => f(a)
            }
    }
}
