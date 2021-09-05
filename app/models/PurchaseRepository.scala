package models

import controllers.helpers.DatabaseExecutionContext
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class PurchaseRepository @Inject()
(@NamedDatabase("shoper") databaseConfigProvider: DatabaseConfigProvider, implicit val ec: DatabaseExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile]
    with UsersComponent {

  override protected val dbConfigProvider: DatabaseConfigProvider = databaseConfigProvider

  import profile.api._

  private val purchases = TableQuery[PurchasesTable]
  private val users = TableQuery[UsersTable]

  def batchInsert(purchases: Seq[Purchase]): Future[Unit] = db.run {
    DBIO.seq(this.purchases ++= purchases)
  }

  def batchDelete(ids: Seq[Long]): Future[Seq[Int]] = db.run {
    DBIO.sequence(ids.map(i => purchases.filter(_.id === i).delete))
  }

  def list(): Future[Seq[PurchaseDTO]] = db.run {
    {
      for {
        (p, u) <- purchases joinLeft users on (_.userId === _.id)
      } yield (p, u)
    }.result
      .map(seq => {
        seq.map(res => {
          val (purchase, user) = res
          PurchaseDTO(Some(purchase.id), purchase.name, purchase.status,
            Some(purchase.createdAt), user.map(_.name).getOrElse(Some("Unknown"))
          )
        })
      })
  }

  def update(id: Long, status: String): Future[Int] = db.run {
    (for {p <- purchases if p.id === id} yield p.status).update(status)
  }

  private class PurchasesTable(tag: Tag) extends Table[Purchase](tag, "purchases") {
    def * = (id, name, status, createdAt, userId) <> (Purchase.tupled, Purchase.unapply)

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def status = column[String]("status")

    def createdAt = column[LocalDateTime]("created_at")

    def userId = column[Option[Long]]("user_id")
  }
}
