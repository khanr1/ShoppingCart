package shop.domain

import shop.Generators.moneyGen


import org.scalacheck.Arbitrary
import squants.market.Money
import weaver.FunSuite
import weaver.discipline.Discipline
import cats.kernel.laws.discipline.MonoidTests

object OrphanSuite extends FunSuite with Discipline {

  given  arbMoney: Arbitrary[Money] = Arbitrary(moneyGen)

  checkAll("Monoid[Money]", MonoidTests[Money].monoid)

}
