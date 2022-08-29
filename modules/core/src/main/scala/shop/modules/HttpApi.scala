package shop.modules

import scala.concurrent.duration.*
import cats.effect.kernel.Async
import cats.syntax.all.*
import com.khanr1.auth.JwtAuthMiddleware
import shop.http.auth.UserAuth.AdminUser
import shop.http.auth.UserAuth.CommonUser
import shop.http.routes.auth.LoginRoutes
import shop.http.routes.auth.LogoutRoutes.apply
import shop.http.routes.auth.LogoutRoutes
import shop.http.routes.auth.UserRoutes
import shop.http.routes.HealthRoutes
import shop.http.routes.BrandRoutes
import shop.http.routes.CategoryRoutes
import shop.http.routes.ItemRoutes
import shop.http.routes.secured.CartRoutes
import shop.http.routes.secured.CheckoutRoutes
import shop.http.routes.secured.OrderRoutes
import org.http4s.CacheDirective.`private`
import shop.http.routes.admin.AdminBrandRoutes
import shop.http.routes.admin.AdminCategoryRoutes
import shop.http.routes.admin.AdminItemRoutes
import org.http4s.HttpRoutes
import shop.http.routes.Version
import org.http4s.server.Router
import org.http4s.server.middleware.AutoSlash
import org.http4s.server.middleware.Timeout
import org.http4s.server.middleware.CORS
import org.http4s.HttpApp
import org.http4s.server.middleware.RequestLogger
import org.http4s.server.middleware.ResponseLogger

abstract class HttpApi[F[_]:Async] private(
  services:Services[F],
  programs:Programs[F],
  security:Security[F]
) {
  private val adminMiddleware = JwtAuthMiddleware[F,AdminUser](
    security.adminJwtAuth.value,security.adminAuth.findUser
  )

  private val usersMiddleware = JwtAuthMiddleware[F,CommonUser](
    security.userJwtAuth.value,security.userAuth.findUser
  )

  //Auth routes
  private val loginRoutes = LoginRoutes[F](security.auth).routes
  private val logoutRoutes= LogoutRoutes[F](security.auth).routes(usersMiddleware)
  private val userRoutes = UserRoutes[F](security.auth).routes

  //Open routes 
  private val healthRoutes = HealthRoutes[F](services.healthCheck).routes
  private val brandRoutes = BrandRoutes[F](services.brands).routes
  private val categoryRoutes = CategoryRoutes[F](services.categories).routes
  private val itemRoutes = ItemRoutes[F](services.items).routes

  //Secured Routes
  private val cartRoutes = CartRoutes[F](services.cart).routes(usersMiddleware)
  private val checkoutRoutes = CheckoutRoutes[F](programs.checkout).routes(usersMiddleware)
  private val orderRoutes = OrderRoutes[F](services.orders).routes(usersMiddleware)

  //AdminRoutes
  private val adminBrandRoutes = AdminBrandRoutes[F](services.brands).routes(adminMiddleware)
  private val adminCategoryRoutes = AdminCategoryRoutes[F](services.categories).routes(adminMiddleware)
  private val adminItemRoutes = AdminItemRoutes[F](services.items).routes(adminMiddleware)

  //combining all routes
  private val openRoutes:HttpRoutes[F]=
    healthRoutes <+> itemRoutes <+> brandRoutes <+> 
    categoryRoutes <+> loginRoutes <+> userRoutes <+>
    logoutRoutes <+> cartRoutes <+> orderRoutes <+> 
    checkoutRoutes

  private val adminRoutes:HttpRoutes[F]=
    adminItemRoutes <+> adminBrandRoutes<+>adminCategoryRoutes

  private val routes:HttpRoutes[F]= Router(
    Version.v1 -> openRoutes,
    Version.v1+"/admin"-> adminRoutes
  )

  private val middleware:HttpRoutes[F]=>HttpRoutes[F] = {
    {(http:HttpRoutes[F]) => AutoSlash(http)
    } andThen{ (http:HttpRoutes[F]) => CORS(http) 
    } andThen{ (http:HttpRoutes[F]) => Timeout(60.seconds)(http)
    }
  }

  private val loggers : HttpApp[F] => HttpApp[F] ={
    { (http:HttpApp[F]) =>
      RequestLogger.httpApp(true,true)(http)
    } andThen { (http:HttpApp[F]) =>
      ResponseLogger.httpApp(true,true)(http)
    }
  }

  val httpApp:HttpApp[F] = loggers(middleware(routes).orNotFound)


}
