package shop.resources

import shop.config.Types.HttpClientConfig
import cats.effect.*
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.Logger

trait MkHttpClient[F[_]] {
    def newEmber(c:HttpClientConfig):Resource[F,Client[F]]
  
}


object MkHttpClient{
    def apply[F[_]:MkHttpClient]:MkHttpClient[F]=summon

    private def showEmberBanner[F[_]:Logger](c:Client[F]):F[Unit]=
            Logger[F].info(s"\n DEBUG ---- HTTP Client started ${c.toString()}")

    given forAsync[F[_]:Async : Logger]:MkHttpClient[F] = 
        new MkHttpClient[F]{
            override def newEmber(c: HttpClientConfig): Resource[F, Client[F]] = 
                EmberClientBuilder.default[F]
                                  .withTimeout(c.timeout)
                                  .withIdleTimeInPool(c.idleTimeInPool)
                                  .build
                                  .evalTap(showEmberBanner(_))
                                  
        }
}