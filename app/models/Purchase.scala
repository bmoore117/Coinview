package models

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future


final case class Purchase(id: Int, userId: Long, coinSlug: String, coinAmount: Double, purchaseDate: Timestamp,
                          purchaseCurrencySlug: String, purchaseCurrencyAmount: Double) {}

final class PurchasesTable(tag: Tag) extends Table[Purchase](tag, "purchases") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def userId = column[Long]("user_id")
  def coinSlug = column[String]("coin_slug")
  def coinAmount = column[Double]("coin_amount")
  def purchaseDate = column[Timestamp]("purchase_date")
  def purchaseCurrencySlug = column[String]("purchase_currency_slug")
  def purchaseCurrencyAmount = column[Double]("purchase_currency_amount")

  def * = (id, userId, coinSlug, coinAmount, purchaseDate, purchaseCurrencySlug, purchaseCurrencyAmount).mapTo[Purchase]
}

@Singleton
class PurchaseDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) {

  val purchases = TableQuery[PurchasesTable]

  def findAll(): Future[Seq[Purchase]] = {
    dbConfigProvider.get.db.run(purchases.result)
  }
}