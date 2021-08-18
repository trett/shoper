package models

import controllers.helpers.DatabaseExecutionContext
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class PurchaseRepository @Inject()(@NamedDatabase("shoper") dbConfigProvider: DatabaseConfigProvider,
                                   implicit val ec: DatabaseExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private val purchases = TableQuery[PurchasesTable]

  def batchInsert(purchases: Seq[Purchase]): Future[Unit] = db.run {
    DBIO.seq(this.purchases ++= purchases)
  }

  def batchDelete(ids: Seq[Long]): Future[Seq[Int]] = db.run {
    DBIO.sequence(ids.map(i => purchases.filter(_.id === i).delete))
  }

  def update(id: Long, status: String): Future[Int] = db.run {
    (for {p <- purchases if p.id === id} yield p.status).update(status)
  }

  def list(): Future[Seq[Purchase]] = db.run {
    purchases.sortBy(_.id).result
  }

  private class PurchasesTable(tag: Tag) extends Table[Purchase](tag, "purchases") {
    def * = (id, name, status) <> (Purchase.tupled, Purchase.unapply)

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def status = column[String]("status")
  }
}
