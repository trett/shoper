package models

import controllers.helpers.DatabaseExecutionContext
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

trait UsersComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  class UsersTable(tag: Tag) extends Table[User](tag, "users") {

    val users = TableQuery[UsersTable]

    def * = (id, email, password, name) <> (User.tupled, User.unapply)

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def email = column[String]("email")

    def password = column[String]("password")

    def name = column[Option[String]]("name")
  }

}

@Singleton
class UserRepository @Inject()
(@NamedDatabase("shoper") databaseConfigProvider: DatabaseConfigProvider, implicit val ec: DatabaseExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]
    with UsersComponent {

  import profile.api._

  override protected val dbConfigProvider: DatabaseConfigProvider = databaseConfigProvider

  val users = TableQuery[UsersTable]

  def save(user: User): Future[Int] = db.run {
    users += user
  }

  def list(): Future[Seq[User]] = db.run {
    users.result
  }

  def update(user: User): Future[Int] = db.run {
    (for {u <- users if u.id === user.id} yield u).update(user)
  }

  def findById(id: Long): Future[Option[User]] = db.run {
    users.filter(_.id === id).result.headOption
  }

  def findByEmail(email: String): Future[Option[User]] = db.run {
    users.filter(_.email === email).result.headOption
  }
}
