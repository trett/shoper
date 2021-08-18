package models

import slick.jdbc.PostgresProfile.api._

object Tables {
  val users = TableQuery[Users]
  val purchases = TableQuery[Purchases]
}

case class User(email: String, password: String)

case class Purchase(id: Long, name: String, status: String)

class Users(tag: Tag) extends Table[User](tag, "users") {
  def * = (email, password) <> (User.tupled, User.unapply)

  def email = column[String]("email", O.PrimaryKey)

  def password = column[String]("password")
}

class Purchases(tag: Tag) extends Table[Purchase](tag, "purchases") {
  def * = (id, name, status) <> (Purchase.tupled, Purchase.unapply)

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name")

  def status = column[String]("status")
}
