package tasks

import java.sql.Timestamp
import javax.inject.Inject

import akka.actor.ActorSystem
import models.coinmarketcap.Ticker
import models.db.{CoinDAO, HistoricalPrice, HistoricalPriceDAO}
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


class FetchPriceTask @Inject()(actorSystem: ActorSystem, ws: WSClient, coinsDao: CoinDAO, pricesDAO: HistoricalPriceDAO)(implicit executionContext: ExecutionContext) {

  coinsDao.findAll().onComplete(results => {
    val coins = results.get

    implicit val tickerReads: Reads[Ticker] = Json.reads[Ticker]

    coins.foreach(coin => {
      actorSystem.scheduler.schedule(0.seconds, 15.minutes) {

        val response = ws.url("https://api.coinmarketcap.com/v1/ticker/" + coin.name).get()

        //possible unhandled case: response status
        val tickerFuture = response.map(response => {Json.fromJson[Ticker](response.json)})

        val priceFuture = tickerFuture.map(ticker => {
          ticker.fold(errors => {
            errors.foreach(tuple => {
              var errorMsg = "Error while parsing: " + tuple._1.toString() + "\nFailed Paths:"
              tuple._2.foreach(error => errorMsg += error.message)
              Logger.error(errorMsg)
            })

            None
          }, instance => Some(HistoricalPrice(coin.slug, new Timestamp(instance.lastUpdated), instance.priceUsd, "USD")))
        })

        for {
          priceOpt <- priceFuture
          value <- priceOpt
        } yield {
          pricesDAO.insert(value)
        }
      }
    })
  })
}
