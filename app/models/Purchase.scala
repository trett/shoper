package models

import play.api.libs.json.{Json, OFormat}

case class Purchase(id: Long, name: String, status: String) {
  implicit val purchaseFormat: OFormat[Purchase] = Json.format[Purchase]
}
