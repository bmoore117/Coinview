package models.db

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

final case class HistoricalPrice(coinSlug: String, price_date: Timestamp, price: Double, priceUnits: String) {}

final class HistoricalPricesTable(tag: Tag) extends Table[HistoricalPrice](tag, "historical_prices") {
  def coinSlug = column[String]("coin_slug")
  def priceDate = column[Timestamp]("price_date")
  def price = column[Double]("price")
  def priceUnits = column[String]("price_units")

  def pk = primaryKey("historical_purchases_pk", (coinSlug, priceDate))

  def * = (coinSlug, priceDate, price, priceUnits).mapTo[HistoricalPrice]
}

@Singleton
class HistoricalPriceDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) {

  val prices = TableQuery[HistoricalPricesTable]
  val db = dbConfigProvider.get.db

  def findAll(): Future[Seq[HistoricalPrice]] = {
    db.run(prices.result)
  }

  def insert(newPrices: HistoricalPrice*): Unit = {
    db.run(prices ++= newPrices)
  }
}