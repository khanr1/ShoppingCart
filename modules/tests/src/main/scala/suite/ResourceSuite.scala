package suite

import weaver.IOSuite
import weaver.scalacheck.Checkers
import weaver.scalacheck.CheckConfig
import cats.effect.*
import cats.effect.IO
import cats.syntax.all.*

abstract class  ResourceSuite extends IOSuite with Checkers{
    //for it: tests, one test is enough
    override def checkConfig: CheckConfig = CheckConfig.default.copy(minimumSuccessful = 1)

    extension (res:Resource[IO,Res]){
        def beforeAll(f:Res=> IO[Unit]):Resource[IO,Res]=res.evalTap(f)
        def afterAll(f: Res=> IO[Unit]):Resource[IO,Res]=
            res.flatTap(x => Resource.make(IO.unit)( _ => f(x)))
    }


  
}
