package models.db

import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

final case class Fiat(slug: String, name: String) {}

final class FiatTable(tag: Tag) extends Table[Fiat](tag, "fiat") {
  def slug = column[String]("slug", O.PrimaryKey)
  def name = column[String]("name")

  def * = (slug, name).mapTo[Fiat]
}

@Singleton
class FiatDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) {

  val fiat = TableQuery[FiatTable]

  def findAll(): Future[Seq[Fiat]] = {
    dbConfigProvider.get.db.run(fiat.result)
  }
}