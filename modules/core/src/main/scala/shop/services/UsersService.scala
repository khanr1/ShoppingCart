package shop.services

import cats.effect._
import cats.syntax.all.*
import shop.domain.ID.*
import shop.domain.AuthDomain.*
import shop.effects.GenUUID
import shop.http.auth.UserAuth.*
import shop.sql.Codecs.*
import skunk.*
import skunk.implicits.*
import shop.domain.ID

trait UsersService[F[_]] {
    def find(username:UserName):F[Option[UserWithPassword]]
    def create(username:UserName,password:EncryptedPassword):F[UserID]
  
}

object UsersService{
    def make[F[_]:GenUUID:MonadCancelThrow](postgres:Resource[F,Session[F]]):UsersService[F]=
        new UsersService[F]{
            import  UserSQL.*
            
            def find(username: UserName): F[Option[UserWithPassword]] = postgres.use{
                s => s.prepare(selectUser).use{
                    pc => pc.option(username).map{
                        case Some(u ~ p) => UserWithPassword(u.id,u.name,p).some
                        case _ => none[UserWithPassword]
                    }
                }
            }

            def create(username: UserName, password: EncryptedPassword): F[UserID] = postgres.use{
                session => session.prepare(createUser).use{
                    pc=> ID.make[F,UserID].flatMap{ id =>
                        pc.execute(User(id,username)~password)
                          .as(id)
                          .recoverWith{
                            case SqlState.UniqueViolation(_)=> UserNameInUser(username).raiseError[F,UserID]
                          }
                        
                    }
                }
            }
        }
}

private object UserSQL{
    val codec:Codec[User ~ EncryptedPassword]=
        (userID ~ username ~ encryptedPassword).imap{
            case i ~ n ~ p => User(i , n) ~ p
        }{ case u ~ p  => u.id ~ u.name ~ p}

    def selectUser:Query[UserName,User ~ EncryptedPassword]=
        sql"SELECT * FROM users WHERE name =$username".query(codec)

    def createUser:Command[User ~ EncryptedPassword]=
        sql"INSERT INTO users VALUES($codec)".command
}
