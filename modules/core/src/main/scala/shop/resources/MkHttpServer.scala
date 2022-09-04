package shop.resources


import cats.effect.kernel.{Resource,Async}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.HttpApp
import org.http4s.server.defaults.Banner
import org.http4s.server.Server
import org.typelevel.log4cats.Logger
import shop.config.Types.HttpServerConfig


trait MkHttpServer[F[_]] {
  def newEmber(
    cfg:HttpServerConfig,
    httpApp:HttpApp[F]
  ):Resource[F,Server]
}


object MkHttpServer{
    def apply[F[_]: MkHttpServer]:MkHttpServer[F]= summon
    
    private def showEmberBanner[F[_]:Logger](s:Server):F[Unit]=
            Logger[F].info(s"\n${Banner.mkString("\n")}\nHTTP Server started at ${s.address}")

    given forAsyncLogger[F[_]: Async: Logger]:MkHttpServer[F] =
        new MkHttpServer[F]{
            def newEmber(cfg: HttpServerConfig, httpApp: HttpApp[F]): Resource[F, Server] = 
                EmberServerBuilder
                    .default[F]
                    .withHost(cfg.host)
                    .withPort(cfg.port)
                    .withHttpApp(httpApp)
                    .build
                    .evalTap(showEmberBanner[F])
        }
}