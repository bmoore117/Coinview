package models

import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

final case class Currency(slug: String) {}

final class CurrencyTable(tag: Tag) extends Table[Currency](tag, "currency") {
  def slug = column[String]("slug", O.PrimaryKey)

  def * = slug.mapTo[Currency]
}

@Singleton
class CurrencyDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) {

  val currencies = TableQuery[CurrencyTable]

  def findAll(): Future[Seq[Currency]] = {
    dbConfigProvider.get.db.run(currencies.result)
  }
}