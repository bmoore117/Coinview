package models

import java.sql.Timestamp

import slick.jdbc.PostgresProfile.api._

final case class HistoricalPrice(coinSlug: String, price_date: Timestamp, price: Double, priceUnits: String) {}

final class HistoricalPricesTable(tag: Tag) extends Table[HistoricalPrice](tag, "historical_prices") {
  def coinSlug = column[String]("coin_slug")
  def priceDate = column[Timestamp]("price_date")
  def price = column[Double]("price")
  def priceUnits = column[String]("price_units")

  def pk = primaryKey("historical_purchases_pk", (coinSlug, priceDate))

  def * = (coinSlug, priceDate, price, priceUnits).mapTo[HistoricalPrice]
}