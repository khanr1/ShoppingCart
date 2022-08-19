package shop





import java.util.UUID
import org.scalacheck.Gen
import shop.domain.BrandDomain.*
import shop.domain.CartDomain.*
import shop.domain.CategoryDomain.*
import shop.domain.CheckOutDomain.*
import shop.domain.ItemDomain.*
import squants.market.Currency.*
import squants.market.Money
import squants.market.USD
import shop.http.auth.UserAuth.User
import shop.domain.AuthDomain.UserID
import shop.domain.AuthDomain.UserName
import shop.http.auth.UserAuth.CommonUser
import shop.domain.OrderDomain.PaymentID
import shop.domain.OrderDomain.OrderID
import shop.domain.PaymentDomain.Payment



object Generators {
    
    //generate nonEmpty string
    val nonEmptyStringGen: Gen[String]=
        Gen
        .chooseNum(21,40)
        .flatMap(n => Gen.buildableOfN[String,Char](n, Gen.alphaChar))
    //generate A from nonempy string if String=> A exist
    def  nesGen[A](f: String=> A):Gen[A]=
        nonEmptyStringGen.map(f)
    //generate A from UUID if UUID=> A exist
    def idGen[A](f:UUID=>A):Gen[A]=
        Gen.uuid.map(f)
    //BrandName,BrandID and Brand generator
    val brandIDGen:Gen[BrandID]=idGen(BrandID.apply)
    val brandNameGen:Gen[BrandName]=nesGen(BrandName.apply)
    val brandGen:Gen[Brand]=for{
        id <- brandIDGen
        name<-brandNameGen
    } yield Brand(id,name)
    //categoryName,categoryID and category generator
    val categoryIDGen:Gen[CategoryID]=idGen(CategoryID.apply)
    val categoryNameGen:Gen[CategoryName]=nesGen(CategoryName.apply)
    val categoryGen:Gen[Category]=for{
        id <- categoryIDGen
        name<- categoryNameGen
    } yield Category(id,name)
    //money generator
    val moneyGen:Gen[Money]=
        Gen.posNum[Long].map(n=> USD(BigDecimal(n)))
    //item generator 
    val itemIdGen:Gen[ItemID]=idGen(ItemID.apply)
    val itemNameGen:Gen[ItemName]=nesGen(ItemName.apply)
    val itemDescription:Gen[ItemDescription]=nesGen(shop.domain.ItemDomain.ItemDescription.apply)
    val itemGen:Gen[Item]=for{
        i<-itemIdGen
        n<-itemNameGen
        d<-itemDescription
        p<-moneyGen
        b<-brandGen
        c<-categoryGen
    } yield Item(i,n,d,p,b,c)
    //cartItem gen
    val quantityGen:Gen[Quantity]=Gen.posNum.map(Quantity.apply)
    val cartItemGen:Gen[CartItem]=for{
        i<- itemGen
        q<- quantityGen
    } yield CartItem(i,q)
    val cartTotalGen: Gen[CartTotal]=for{
        i<-Gen.nonEmptyListOf(cartItemGen)
        t<-moneyGen
    } yield CartTotal(i,t)
    val itemMapGen:Gen[(ItemID,Quantity)]=for{
        i<-itemIdGen
        q<-quantityGen
    } yield i->q
    val cartGen:Gen[Cart]=Gen.nonEmptyMap(itemMapGen).map(Cart.apply)
    //card generator
    val cardNameGen:Gen[CardName]=
        Gen.stringOf(
            Gen.oneOf(('a' to 'z') ++ ('A' to 'Z'))
            
        ).map{ x=> CardName.unSafeApply(x)}
    private def sized(size: Int): Gen[Long] = {
        def go(s: Int, acc: String): Gen[Long] =
        Gen.oneOf(1 to 9).flatMap { n =>
            if (s == size) acc.toLong
            else go(s + 1, acc + n.toString)
        }

        go(0, "")
    }
    val cardGen: Gen[Card] =
    for {
      n <- cardNameGen
      u <- sized(16).map(x => CardNumber.unSafeApply(x))
      x <- sized(4).map(x => CardExpiration.unSafeApply(x.toString()))
      c <- sized(3).map(x => CardCVV.unSafeApply(x.toInt))
    } yield Card(n, u, x, c)
    //User Gen
    val userIdGen:Gen[UserID]=idGen(UserID.apply)
    val userNameGen:Gen[UserName]=nesGen(UserName.apply)
    val userGen:Gen[User]=for{
        i<- userIdGen
        n<- userNameGen
    } yield User(i,n)
    val commonUserGen:Gen[CommonUser]=userGen.map(CommonUser.apply)
    //payment 
    val paymentIdGen: Gen[PaymentID] =
        idGen(PaymentID.apply)
      val paymentGen: Gen[Payment] =
    for {
      i <- userIdGen
      m <- moneyGen
      c <- cardGen
    } yield Payment(i, m, c)
    //order
    val orderIdGen:Gen[OrderID]=idGen(OrderID.apply)

    



}