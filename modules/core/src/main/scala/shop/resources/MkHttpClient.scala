package shop.resources

import shop.config.Types.HttpClientConfig
import cats.effect.*
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder

trait MkHttpClient[F[_]] {
    def newEmber(c:HttpClientConfig):Resource[F,Client[F]]
  
}


object MkHttpClient{
    def apply[F[_]:MkHttpClient]:MkHttpClient[F]=summon

    given forAsync[F[_]:Async]:MkHttpClient[F] = 
        new MkHttpClient[F]{
            override def newEmber(c: HttpClientConfig): Resource[F, Client[F]] = 
                EmberClientBuilder.default[F]
                                  .withTimeout(c.timeout)
                                  .withIdleTimeInPool(c.idleTimeInPool)
                                  .build
        }
}