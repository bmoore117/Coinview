package models

import slick.jdbc.PostgresProfile.api._

final case class Currency(slug: String) {}

final class CurrencyTable(tag: Tag) extends Table[Currency](tag, "currency") {
  def slug = column[String]("slug", O.PrimaryKey)

  def * = slug.mapTo[Currency]
}
