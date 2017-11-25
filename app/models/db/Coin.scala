package models.db

import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

final case class Coin(slug: String, name: String) {}

final class CoinTable(tag: Tag) extends Table[Coin](tag, "coin") {
  def slug = column[String]("slug", O.PrimaryKey)
  def name = column[String]("name")

  def * = (slug, name).mapTo[Coin]
}

@Singleton
class CoinDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) {

  val coins = TableQuery[CoinTable]

  def findAll(): Future[Seq[Coin]] = {
    dbConfigProvider.get.db.run(coins.result)
  }
}