package shop.domain

import monocle.Iso
import io.circe.Encoder
import io.circe.Json
import cats.kernel.Eq
import cats.Show

object HealthCheckDomain{

  enum Status{

      case Okay extends Status
      case Unreachable extends Status
  }

  object Status{
        val _Bool:Iso[Status,Boolean]=Iso[Status,Boolean]{
          case Status.Okay => true
          case Status.Unreachable => false
        }(if (_) Status.Okay else Status.Unreachable)

        given encoder:Encoder[Status]= s => Encoder.encodeString.apply(s.toString)
        given eq:Eq[Status]=Eq.fromUniversalEquals
        given show:Show[Status]=Show.fromToString
        
  }

  opaque type RedisStatus= Status
  object RedisStatus{
     def apply(s:Status):RedisStatus=s
     extension (rs:RedisStatus) def value:Status= rs

     given encoder:Encoder[RedisStatus]= new Encoder[RedisStatus]{
            def apply(rs:RedisStatus):Json =Json.obj(
                ("status",Json.fromString(rs.value.toString))
            )
        }
  }
  opaque type PostgresStatus= Status
  object PostgresStatus{
     def apply(s:Status):PostgresStatus=s
     extension (rs:PostgresStatus) def value:Status= rs

     given encoder:Encoder[PostgresStatus]= new Encoder[PostgresStatus]{
            def apply(rs:PostgresStatus):Json =Json.obj(
                ("status",Json.fromString(rs.value.toString))
            )
        }
  }

  final case class AppStatus(redisStatus:RedisStatus,postgresStatus:PostgresStatus)

  object AppStatus{
    given encoder:Encoder[AppStatus]=Encoder.forProduct2(
        "redisStatus",
        "postgresStatus"
    )( x => (x.redisStatus,x.postgresStatus))
  }


}
