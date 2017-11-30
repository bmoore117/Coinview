package models.db

import java.sql.Timestamp
import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json._
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


final case class Purchase(userId: Long, coinSlug: String, coinAmount: Double, purchaseDate: Timestamp,
                          purchaseCurrencySlug: String, purchaseCurrencyAmount: Double)

object Purchase extends ((Long, String, Double, Timestamp, String, Double) => Purchase) {

  implicit val timestampFormat: Format[Timestamp] = new Format[Timestamp] {
    def writes(t: Timestamp): JsNumber = JsNumber(t.getTime)
    def reads(json: JsValue): JsResult[Timestamp] = JsSuccess(new Timestamp(json.as[Long]))
  }

  implicit val tickerReads: Reads[Purchase] = Json.reads[Purchase]
}

final class PurchasesTable(tag: Tag) extends Table[Purchase](tag, "purchases") {
  def userId = column[Long]("user_id")
  def coinSlug = column[String]("coin_slug")
  def coinAmount = column[Double]("coin_amount")
  def purchaseDate = column[Timestamp]("purchase_date")
  def purchaseCurrencySlug = column[String]("purchase_currency_slug")
  def purchaseCurrencyAmount = column[Double]("purchase_currency_amount")

  def * = (userId, coinSlug, coinAmount, purchaseDate, purchaseCurrencySlug, purchaseCurrencyAmount).mapTo[Purchase]
}

@Singleton
class PurchaseDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  val purchases = TableQuery[PurchasesTable]
  val db = dbConfigProvider.get.db

  def findAll(): Future[Seq[Purchase]] = {
    db.run(purchases.result).transform {
      case Success(s) => Success(s)
      case Failure(exception) => Logger.error("findAll failed", exception)
        Failure(exception)
    }
  }

  def insert(newPurchases: Purchase*): Future[PostgresProfile.InsertActionExtensionMethods[PurchasesTable#TableElementType]#MultiInsertResult] = {
    db.run(purchases ++= newPurchases).transform {
      case Success(s) => Success(s)
      case Failure(exception) => Logger.error("insert failed", exception)
        Failure(exception)
    }
  }
}