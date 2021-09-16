package models

import play.api.libs.json.Json
import play.api.libs.json.OFormat

import java.time.LocalDateTime

case class Purchase(
    id: Long,
    name: String,
    status: String,
    createdAt: LocalDateTime,
    userId: Option[Long]
) {
  implicit val purchaseFormat: OFormat[Purchase] = Json.format[Purchase]
}
