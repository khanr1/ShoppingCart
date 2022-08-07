import sbt._
object Dependencies{

    object Version{
        val cats       = "2.7.0"
        val catsEffect = "3.3.11"
        val catsRetry  = "3.1.0"
        val catsLogs   = "2.2.0"
        val circe      = "0.14.1"
        val http4s     = "0.23.11"
        val squants    = "1.8.3"
        val monocle    = "3.1.0"
        val skunk      = "0.3.1"
        val redis4cats = "1.2.0"
        val jwtAuth    = "0.1.0-SNAPSHOT"
        val javaxCrypto   = "1.0.1"
    }
    
    object Library{ 
          
        val cats          = "org.typelevel"      %% "cats-core"           % Version.cats
        val catsEffect    = "org.typelevel"      %% "cats-effect"         % Version.catsEffect
        val catsRetry     = "com.github.cb372"   %% "cats-retry"          % Version.catsRetry
        val catsLogs      = "org.typelevel"      %% "log4cats-core"       % Version.catsLogs
        val squants       = "org.typelevel"      %% "squants"             % Version.squants
        val circe         = "io.circe"           %% "circe-core"          % Version.circe
        val monocle       = "dev.optics"         %% "monocle-core"        % Version.monocle
        val http4s        = "org.http4s"         %% "http4s-core"         % Version.http4s
        val http4sDsl     = "org.http4s"         %% "http4s-dsl"          % Version.http4s
        val http4sCirce   = "org.http4s"         %% "http4s-circe"        % Version.http4s
        val http4sServer  = "org.http4s"         %% "http4s-ember-server" % Version.http4s
        val http4sClient  = "org.http4s"         %% "http4s-ember-client" % Version.http4s
        val skunkCore     = "org.tpolecat"       %% "skunk-core"          % Version.skunk
        val skunkCirce    = "org.tpolecat"       %% "skunk-circe"         % Version.skunk
        val redis4catsCor = "dev.profunktor"     %% "redis4cats-core"     % Version.redis4cats
        val redis4catsEff = "dev.profunktor"     %% "redis4cats-effects"  % Version.redis4cats
        val redis4catsLog = "dev.profunktor"     %% "redis4cats-log4cats" % Version.redis4cats
        val jwtAuth       = "com.khanr1"         %%  "jwt-auth-http4s"    % Version.jwtAuth      
        val javaxCrypto   = "javax.xml.crypto"    % "jsr105-api"          % Version.javaxCrypto

    }
}