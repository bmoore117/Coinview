package models

import slick.jdbc.PostgresProfile.api._

final case class Fiat(slug: String) {}

final class FiatTable(tag: Tag) extends Table[Fiat](tag, "fiat") {
  def slug = column[String]("slug", O.PrimaryKey)

  def * = slug.mapTo[Fiat]
}
