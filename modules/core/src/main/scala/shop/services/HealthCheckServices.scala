package shop.services


import scala.concurrent.duration.*

import shop.domain.HealthCheckDomain.AppStatus
import cats.effect.kernel.*
import cats.effect._
import cats.effect.implicits._
import cats.syntax.all._
import dev.profunktor.redis4cats.RedisCommands
import skunk.*
import skunk.syntax.all.*
import skunk.codec.all._
import shop.domain.HealthCheckDomain.*

trait HealthCheckServices[F[_]] {
  def status:F[AppStatus]
}

object HealthCheckServices{
  def make[F[_]:Temporal](
    postgres:Resource[F,Session[F]],
    redis:RedisCommands[F,String,String]
    ):HealthCheckServices[F]= new HealthCheckServices[F]{
      val q:Query[skunk.Void,Int]=sql"SELECT pid FROM pg_stat_activity".query(int4)
      val redisHealth:F[RedisStatus]= 
        redis.ping.map(_.nonEmpty)
            .timeout(1.second)
            .map(Status._Bool.reverseGet)
            .orElse(Status.Unreachable.pure[F].widen)
            .map(RedisStatus(_))
      val postgresHealth:F[PostgresStatus]= 
        postgres.use(_.execute(q))
              .map(_.nonEmpty)
              .timeout(1.second)
              .map(Status._Bool.reverseGet)
              .orElse(Status.Unreachable.pure[F].widen)
              .map(PostgresStatus(_))
      val status:F[AppStatus]=
        (
          redisHealth,
          postgresHealth
        ).parMapN(AppStatus.apply)
    }
  
}
