package shop.sql

import skunk.*
import skunk.codec.all.* 
import shop.domain.BrandDomain.*
import shop.domain.CategoryDomain.*
import shop.domain.ItemDomain.*
import squants.market.Money
import squants.market.USD
import shop.domain.OrderDomain.OrderID
import shop.domain.OrderDomain.PaymentID
import shop.domain.AuthDomain.UserID
import shop.domain.AuthDomain.UserName
import shop.domain.AuthDomain.EncryptedPassword

object Codecs {
  //BrandDomain
  val brandID:Codec[BrandID]=uuid.imap[BrandID](BrandID(_))(_.value)
  val brandName:Codec[BrandName]=varchar.imap[BrandName](BrandName(_))(_.value)

  //CategoryDomain
  val categoryID:Codec[CategoryID]=uuid.imap[CategoryID](CategoryID(_))(_.value)
  val categoryName:Codec[CategoryName]=varchar.imap[CategoryName](CategoryName(_))(_.value)

  //ItemDomain
  val itemID: Codec[ItemID]=uuid.imap[ItemID](ItemID(_))(_.value)
  val itemDesc:Codec[ItemDescription]=varchar.imap[ItemDescription](ItemDescription(_))(_.value)
  val itemName:Codec[ItemName]=varchar.imap[ItemName](ItemName(_))(_.value)
  
  //Money
  val money : Codec[Money]=numeric.imap[Money](USD(_))(_.amount)

  //OrderDomain
  val orderID :Codec[OrderID]=uuid.imap(OrderID(_))(_.value)
  val paymentID:Codec[PaymentID]=uuid.imap(PaymentID(_))(_.value)
  
  //User
  val userID:Codec[UserID]=uuid.imap(UserID(_))(_.value)
  val username:Codec[UserName]=varchar.imap(UserName(_))(_.value)
  val encryptedPassword:Codec[EncryptedPassword]=varchar.imap(EncryptedPassword(_))(_.value)
  



}
