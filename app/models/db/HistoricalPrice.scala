package models.db

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

final case class HistoricalPrice(coinSlug: String, price_date: Timestamp, price: Double, priceUnits: String) {}

final class HistoricalPricesTable(tag: Tag) extends Table[HistoricalPrice](tag, "historical_prices") {
  def coinSlug = column[String]("coin_slug")
  def priceDate = column[Timestamp]("price_date")
  def price = column[Double]("price")
  def priceUnits = column[String]("price_units")

  def pk = primaryKey("historical_purchases_pk", (coinSlug, priceDate))

  def * = (coinSlug, priceDate, price, priceUnits).mapTo[HistoricalPrice]
}

//TODO: set up custom execution context for db

@Singleton
class HistoricalPriceDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  val prices = TableQuery[HistoricalPricesTable]
  val db = dbConfigProvider.get.db

  def findAll(): Future[Seq[HistoricalPrice]] = {
    db.run(prices.result).transform {
      case Success(s) => Success(s)
      case Failure(exception) => Logger.error("findAll failed", exception)
        Failure(exception)
    }
  }

  def insert(newPrices: HistoricalPrice*): Future[PostgresProfile.InsertActionExtensionMethods[HistoricalPricesTable#TableElementType]#MultiInsertResult] = {
    db.run(prices ++= newPrices).transform {
      case Success(s) => Success(s)
      case Failure(exception) => Logger.error("insert failed", exception)
        Failure(exception)
    }
  }
}