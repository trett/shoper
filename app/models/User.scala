package models

import play.api.libs.json.{Json, OFormat}

case class User(email: String, password: String, name: Option[String]) {
  implicit val userFormat: OFormat[User] = Json.format[User]
}
