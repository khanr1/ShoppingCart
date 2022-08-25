package shop.domain

import java.util.UUID


import shop.Generators.*
import shop.optics.IsUUID.*
import monocle.law.discipline._
import org.scalacheck.{ Arbitrary, Cogen, Gen }
import weaver.FunSuite
import weaver.discipline.Discipline
import shop.domain.BrandDomain.BrandID
import shop.domain.CategoryDomain.CategoryID
import shop.domain.HealthCheckDomain.Status
import shop.optics.IsUUID

object OpticsSuite extends FunSuite with Discipline {

  implicit val arbStatus: Arbitrary[Status] =
    Arbitrary(Gen.oneOf(Status.Okay, Status.Unreachable))

  implicit val uuidCogen: Cogen[UUID] =
    Cogen[(Long, Long)].contramap { uuid =>
      uuid.getLeastSignificantBits -> uuid.getMostSignificantBits
    }

  implicit val brandIdArb: Arbitrary[BrandID] =
    Arbitrary(brandIDGen)

  implicit val brandIdCogen: Cogen[BrandID] =
    Cogen[UUID].contramap[BrandID](_.value)

  implicit val catIdArb: Arbitrary[CategoryID] =
    Arbitrary(categoryIDGen)

  implicit val catIdCogen: Cogen[CategoryID] =
    Cogen[UUID].contramap[CategoryID](_.value)

  checkAll("Iso[Status._Bool]", IsoTests(Status._Bool))

  // we don't really need to test these as they are derived, just showing we can
  //checkAll("IsUUID[UUID]", IsoTests(IsUUID[UUID]._UUID))
  checkAll("IsUUID[BrandId]", IsoTests(IsUUID[BrandID]._UUID))
  checkAll("IsUUID[CategoryId]", IsoTests(IsUUID[CategoryID]._UUID))

}