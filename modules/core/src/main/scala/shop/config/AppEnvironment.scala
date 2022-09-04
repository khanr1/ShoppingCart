package shop.config

import ciris.*
import cats.*
import cats.syntax.all

enum AppEnvironment{
  case Test extends AppEnvironment
  case Prod extends AppEnvironment

}

object AppEnvironment{

  def apply(str:String):Option[AppEnvironment]= str match
      case "Test" => Some(Test)
      case "Prod" => Some(Prod)
      case _ => None   
  

 
  given configDecoder:ConfigDecoder[String,AppEnvironment]=ConfigDecoder[String].mapOption("AppEnvironment"){AppEnvironment(_)}

  given show:Show[AppEnvironment]=Show.fromToString
}