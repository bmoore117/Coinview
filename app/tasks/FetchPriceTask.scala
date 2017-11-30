package tasks

import java.sql.Timestamp
import javax.inject.Inject

import akka.actor.ActorSystem
import models.db.{CoinDAO, HistoricalPrice, HistoricalPriceDAO}
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


case class Ticker(price_usd: String, last_updated: String)
class FailedHTTPRequest(message: String) extends Exception
class FailedJSONParse(message: String) extends Exception

class FetchPriceTask @Inject()(actorSystem: ActorSystem, ws: WSClient, coinsDao: CoinDAO, pricesDAO: HistoricalPriceDAO)(implicit executionContext: ExecutionContext) {

  /*
  We fetch all coin types, which returns a future, then unwrap that future's result list and schedule a job for each
  item in it.

  Each job:
      Makes web request
        |- Success
        |      |- Transforms json to Ticker
        |          |- Success
        |          |   |- Transforms Ticker to Future[MultiInsertResult]
        |          |- Failure
        |              |- Logs Exception
        |- Failure
            |- Logs Exception
   */

  coinsDao.findAll().map(coins => {
    coins.foreach(coin => {
      actorSystem.scheduler.schedule(0.seconds, 15.minutes) {
        getPriceJson(transformToAPI(coin.name)).map(jsOpt =>
          jsOpt.map(js =>
            jsonToTicker(js).map(ticker =>
              pricesDAO.insert(HistoricalPrice(coin.slug, new Timestamp(ticker.last_updated.toLong*1000), ticker.price_usd.toDouble, "USD"))))
        )
      }
    })
  })

  implicit val tickerReads: Reads[Ticker] = Json.reads[Ticker]

  def transformToAPI(coinName: String): String = {
    coinName.toLowerCase.replace(" ", "-")
  }

  def getPriceJson(coin: String): Future[Try[JsValue]] = {
    val response = ws.url("https://api.coinmarketcap.com/v1/ticker/" + coin).get()
    response.map(resp => if(resp.status == 200) {
      Success(resp.json)
    } else {
      val message = "Error: " + resp.status + ": " + resp.statusText
      val failure = new FailedHTTPRequest(message)
      Logger.error(message, failure)
      Failure(failure)
    })
  }

  def jsonToTicker(jsValue: JsValue): Try[Ticker] = {
    jsValue match {
      case JsArray(values) =>
        val result = Json.fromJson[Ticker](values(0))

        result.fold(errors => {
          var errorMsg = ""
          errors.foreach(tuple => {
            errorMsg += "Error while parsing: " + tuple._1.toString() + "\nFailed Paths: "
            tuple._2.foreach(error => errorMsg += "\t" + error.message)
            errorMsg += "\n"
          })
          val failure = new FailedJSONParse(errorMsg)
          Logger.error(errorMsg, failure)
          Failure(failure)
        }, {
          ticker => Success(ticker)
        })

      case _ =>
        val message = "Error: unknown json response format"
        val failure = new FailedJSONParse(message)
        Logger.error(message, failure)
        Failure(failure)
    }
  }
}
