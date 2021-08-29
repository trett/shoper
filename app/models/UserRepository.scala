package models

import controllers.helpers.{DatabaseExecutionContext, PasswordHelper}
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class UserRepository @Inject()(@NamedDatabase("shoper") dbConfigProvider: DatabaseConfigProvider,
                               implicit val ec: DatabaseExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private val users = TableQuery[UsersTable]

  def addUser(user: User): Future[Int] = db.run {
    users += user
  }

  def list(): Future[Seq[User]] = db.run {
    users.result
  }

  def updatePassword(email: String, password: String): Future[Int] = db.run {
    (for {user <- users if user.email === email} yield user.password)
      .update(PasswordHelper.hashPassword(password))
  }

  def findByEmail(email: String): Future[Option[User]] = db.run {
    users.filter(_.email === email).result.headOption
  }

  private class UsersTable(tag: Tag) extends Table[User](tag, "users") {
    def * = (email, password, name) <> (User.tupled, User.unapply)

    def email = column[String]("email", O.PrimaryKey)

    def password = column[String]("password")

    def name = column[Option[String]]("name")
  }
}
