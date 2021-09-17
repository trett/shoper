package models

import play.api.libs.json.Json
import play.api.libs.json.OFormat

case class User(id: Long, login: String, email: Option[String], password: String, name: Option[String]) {
  implicit val userFormat: OFormat[User] = Json.format[User]
}
