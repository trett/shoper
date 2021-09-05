package models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime


case class PurchaseDTO(id: Option[Long], name: String, status: String,
                       createdAt: Option[LocalDateTime], userName: Option[String]) {

  implicit val userFormat: OFormat[PurchaseDTO] = Json.format[PurchaseDTO]
}
