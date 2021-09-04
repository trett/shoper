package controllers

import controllers.helpers.RequestHelper.{PermissionCheckAction, process}
import controllers.helpers.{DatabaseExecutionContext, UserAction, UserRequest}
import models.{Purchase, PurchaseRepository}
import play.api.Logger
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Reads.pure
import play.api.libs.json._
import play.api.mvc._

import javax.inject._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

case class PurchaseRequest(newItems: Seq[Purchase], idsForDelete: Seq[Long])

case class UpdateRequest(id: Long, status: String)

@Singleton
class PurchaseController @Inject()
(val controllerComponents: ControllerComponents,
 val userAction: UserAction,
 val purchaseRepository: PurchaseRepository,
 implicit val ec: DatabaseExecutionContext)
  extends BaseController {

  val logger: Logger = Logger(getClass.getCanonicalName)

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
    userRequest: UserRequest[AnyContent] =>
      userRequest.user.map(process {
        user => Ok(views.html.index(user)(userRequest.request))
      })
  }

  def load(): Action[AnyContent] = userAction.andThen(PermissionCheckAction).async(userAction.parser) {
    implicit request =>
      logger.info(s"Start loading purchases")
      purchaseRepository.list().map {
        result =>
          logger.info(s"Loaded ${result.size} items")
          Ok(Json.toJson(result))
      }
  }

  def update(): Action[JsValue] = userAction(parse.json).andThen(PermissionCheckAction) {
    userRequest: UserRequest[JsValue] =>
      userRequest.request.body.validate[UpdateRequest]
        .fold(
          errors => {
            BadRequest(Json.obj("message" -> JsError.toJson(errors)))
          },
          updateRequest => {
            logger
              .info(s"Start updating purchase: [id: ${updateRequest.id}, status: ${updateRequest.status}]")
            purchaseRepository.update(updateRequest.id, updateRequest.status)
            logger
              .info(s"End of updating purchase: [id: ${updateRequest.id}, status: ${updateRequest.status}]")
            Ok
          }
        )
  }

  def save(): Action[JsValue] = userAction(parse.json).andThen(PermissionCheckAction) {
    userRequest: UserRequest[JsValue] =>
      userRequest.request.body.validate[PurchaseRequest]
        .fold(
          errors => {
            BadRequest(Json.obj("message" -> JsError.toJson(errors)))
          },
          items => {
            logger
              .info(s"Start updating purchases: new: [${items.newItems}], delete: [${items.idsForDelete}]")
            Await.result(Future.sequence(List(
              purchaseRepository.batchInsert(items.newItems),
              purchaseRepository.batchDelete(items.idsForDelete))),
              Duration.Inf
            )
            logger
              .info(s"End of updating purchases:new: [${items.newItems}], delete: [${items.idsForDelete}]")
            Created
          }
        )
  }
}
