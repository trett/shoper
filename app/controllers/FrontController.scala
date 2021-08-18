package controllers

import controllers.helpers.AjaxHelper.PermissionCheckAction
import controllers.helpers.{UserAction, UserRequest}
import models.Purchase
import models.Tables.purchases
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Reads.pure
import play.api.libs.json._
import play.api.mvc._
import play.db.NamedDatabase
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

case class PurchaseRequest(newItems: Seq[Purchase], idsForDelete: Seq[Long])

case class UpdateRequest(id: Long, status: String)

@Singleton
class FrontController @Inject()
(@NamedDatabase("shoper") protected val dbConfigProvider: DatabaseConfigProvider,
 val controllerComponents: ControllerComponents, val userAction: UserAction)
(implicit ec: ExecutionContext)
  extends BaseController with HasDatabaseConfigProvider[PostgresProfile] {

  implicit val purchasesWrites: Writes[Purchase] = Json.writes[Purchase]

  implicit val purchaseReads: Reads[Purchase] = (
    (JsPath \ "id").read[Long].orElse(pure(0)) and
      (JsPath \ "name").read[String] and
      (JsPath \ "status").read[String]
    ) (Purchase.apply _)

  implicit val purchaseRequestReads: Reads[PurchaseRequest] = (
    (JsPath \ "newItems").read[Seq[Purchase]] and
      (JsPath \ "idsForDelete").read[Seq[Long]]
    ) (PurchaseRequest.apply _)

  implicit val updateRequestReads: Reads[UpdateRequest] = Json.reads[UpdateRequest]

  def index(): Action[AnyContent] = userAction.async {
    userReq: UserRequest[AnyContent] =>
      userReq.user.map(user => user.map(u => Ok(views.html.index(u)(userReq.request))))
        .getOrElse(Future {
          Redirect(routes.LoginController.loginForm())
        })
  }

  def loadItems(): Action[AnyContent] = userAction.andThen(PermissionCheckAction).async(userAction.parser) {
    implicit request =>
      val result = db.run(purchases.result)
      result.map { r =>
        Ok(Json.toJson(r))
      }
  }

  def updateStatus(): Action[JsValue] = userAction(parse.json).andThen(PermissionCheckAction) {
    userReq: UserRequest[JsValue] =>
      userReq.request.body.validate[UpdateRequest]
        .fold(
          errors => {
            BadRequest(Json.obj("message" -> JsError.toJson(errors)))
          },
          updateRequest => {
            val q = for {p <- purchases if p.id === updateRequest.id} yield p.status
            db.run(q.update(updateRequest.status))
            Ok("Success")
          }
        )
  }

  def saveData(): Action[JsValue] = userAction(parse.json).andThen(PermissionCheckAction) {
    userReq: UserRequest[JsValue] =>
      val jsonRequest = userReq.request.body
      jsonRequest.validate[PurchaseRequest]
        .fold(
          errors => {
            BadRequest(Json.obj("message" -> JsError.toJson(errors)))
          },
          items => {
            val insertQuery = (purchases ++= items.newItems)
              .transactionally
            val deleteQuery = DBIO.sequence(items.idsForDelete
              .map(i => purchases.filter(_.id === i).delete))
              .transactionally
            db.run(DBIO.seq(insertQuery, deleteQuery))
            Ok("Success")
          }
        )
  }
}
