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


case class Ticker(price_usd: String, last_updated: String) {}

class FetchPriceTask @Inject()(actorSystem: ActorSystem, ws: WSClient, coinsDao: CoinDAO, pricesDAO: HistoricalPriceDAO)(implicit executionContext: ExecutionContext) {

  implicit val tickerReads: Reads[Ticker] = Json.reads[Ticker]

  def transformToAPI(coinName: String): String = {
    coinName.toLowerCase.replace(" ", "-")
  }

  def getPriceJson(coin: String): Future[Option[JsValue]] = {
    val response = ws.url("https://api.coinmarketcap.com/v1/ticker/" + coin).get()
    response.map(resp => if(resp.status == 200) {
      Some(resp.json)
    } else {
      Logger.error("Error: " + resp.status + ": " + resp.statusText)
      None
    })
  }

  def jsonToTicker(jsValue: JsValue): Option[Ticker] = {
    jsValue match {
      case JsArray(values) =>
        val result = Json.fromJson[Ticker](values(0))

        result.fold(errors => {
          errors.foreach(tuple => {
            var errorMsg = "Error while parsing: " + tuple._1.toString() + "\nFailed Paths: "
            tuple._2.foreach(error => errorMsg += error.message)
            Logger.error(errorMsg)
          })
          None
        }, ticker => Some(ticker))

      case _ =>
        Logger.error("Error: unknown json response format")
        None
    }
  }

  coinsDao.findAll().map(coins => {

    coins.foreach(coin => {
      actorSystem.scheduler.schedule(0.seconds, 15.minutes) {

        getPriceJson(transformToAPI(coin.name)).map(jsOpt => {
          val resultTree = jsOpt.map(js =>
            jsonToTicker(js).map(ticker =>
              HistoricalPrice(coin.slug, new Timestamp(ticker.last_updated.toLong*1000), ticker.price_usd.toDouble, "USD")))

          // resultTree encapsulates the possibility that the original fetching of the price json failed, as well as
          // the possibility that converting the received json to a Ticker failed. Strip off the opts and store the
          // price, if present
          for {
            priceOpt <- resultTree
            b <- priceOpt
          } {
            pricesDAO.insert(b)
          }
        })
      }
    })
  })
}
