package shop.config

import scala.concurrent.duration.FiniteDuration
import cats.Show

object Types {
  opaque type ShoppingCartExpiration = FiniteDuration
  object ShoppingCartExpiration{
    def apply(fd:FiniteDuration):ShoppingCartExpiration=fd
    extension (se:ShoppingCartExpiration) def value:FiniteDuration=se
  }
  opaque type TokenExpiration= FiniteDuration
  object TokenExpiration{
    def apply(fd:FiniteDuration):TokenExpiration=fd
    extension (te:TokenExpiration){
      def value:FiniteDuration=te
    }
  }
  opaque type PasswordSalt= String
  object PasswordSalt{
    def apply(str:String):PasswordSalt=str
    extension (ps:PasswordSalt){
      def value:String=ps
    }
    given show:Show[PasswordSalt]=Show.show(ps=> ps.value)
      
  }
  opaque type JwtAccessTokenKeyConfig= String
  object JwtAccessTokenKeyConfig{
    def apply(str:String):JwtAccessTokenKeyConfig=str
    extension (ps:JwtAccessTokenKeyConfig){
      def value:String=ps
    }
    given show:Show[JwtAccessTokenKeyConfig]=Show.show(ps=> ps.value)
      
  }

  opaque type PaymentURI=String
  object PaymentURI{
    def apply(str:String):PaymentURI=str
    extension (pc:PaymentURI){
      def  value:String=pc
    }
  }

  opaque type PaymentConfig=PaymentURI
  object PaymentConfig{
    def apply(puri:PaymentURI):PaymentConfig=puri
    extension (pc:PaymentConfig){
      def  uri:PaymentURI=pc
    }
  }
  
}
