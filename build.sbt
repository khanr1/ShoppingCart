import  Dependencies._
ThisBuild/ scalaVersion := "3.1.1"
ThisBuild/ version      := "0.0.1"
ThisBuild/ organization := "com.khanr1"
ThisBuild/ organizationName := "khanr1"


lazy val root = (project in file("."))
    .settings(
        name :="shopping-cart"
    )
    .aggregate(core,tests)

lazy val core = (project in file("./modules/core"))
    .settings(
        name:= "shopping-cart-core",
        libraryDependencies ++=Seq(
            Library.cats,
            Library.catsEffect,
            Library.catsRetry,  
            Library.catsLogs,
            Library.circe,
            Library.monocle,
            Library.squants,
            Library.http4s,
            Library.http4sDsl,
            Library.http4sCirce,
            Library.http4sServer,
            Library.http4sClient,
            Library.skunkCirce,
            Library.skunkCore,
            Library.redis4catsEff,
            Library.redis4catsLog,
            Library.jwtAuth,
            Library.javaxCrypto,
            Library.ciris,
            Library.logback % Runtime,
        )
    )

lazy val tests = (project in file("./modules/tests"))
    .configs(IntegrationTest)
    .settings(
        name:="shopping-cart-tests",
        testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
        Defaults.itSettings,
        libraryDependencies ++=Seq(
            Library.catsLogsNoOp,
            Library.catsLaws,
            Library.monocleLaws,
            Library.weaverCats,
            Library.weaverDiscipline,
            Library.weaverScalaCheck
        )
    )
    .dependsOn(core)