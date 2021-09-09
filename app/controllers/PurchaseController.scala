package controllers

import controllers.helpers.RequestHelper.{PermissionCheckAction, REDIRECT_TO_LOGIN, process, jsonErrors}
import controllers.helpers.{DatabaseExecutionContext, UserAction, UserRequest}
import models.{Purchase, PurchaseDTO, PurchaseRepository, User}
import play.api.Logger
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import play.api.mvc._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject._
import scala.concurrent.Future

case class PurchaseRequest(newItems: Seq[PurchaseDTO], idsForDelete: Seq[Long])

case class UpdateRequest(id: Long, status: String)

@Singleton
class PurchaseController @Inject()
(val controllerComponents: ControllerComponents,
 val userAction: UserAction,
 val purchaseRepository: PurchaseRepository,
 implicit val ec: DatabaseExecutionContext)
  extends BaseController {

  val logger: Logger = Logger(getClass.getCanonicalName)
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  implicit val customLocalDateTimeWrites: Writes[LocalDateTime] = (o: LocalDateTime) => JsString(formatter.format(o))
  implicit val purchasesWrites: Writes[PurchaseDTO] = Json.writes[PurchaseDTO]

  implicit val purchaseReads: Reads[PurchaseDTO] = (
    (JsPath \ "id").readNullable[Long] and
      (JsPath \ "name").read[String] and
      (JsPath \ "status").read[String] and
      (JsPath \ "createdAt").readNullable[LocalDateTime] and
      (JsPath \ "userName").readNullable[String]
    ) (PurchaseDTO.apply _)

  implicit val purchaseRequestReads: Reads[PurchaseRequest] = (
    (JsPath \ "newItems").read[Seq[PurchaseDTO]] and
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

  def update(): Action[JsValue] = userAction.andThen(PermissionCheckAction).async(parse.json) {
    userRequest: UserRequest[JsValue] =>
      userRequest.request.body.validate[UpdateRequest]
        .fold(jsonErrors(),
          updateRequest => {
            logger
              .info(s"Start updating purchase: [id: ${updateRequest.id}, status: ${updateRequest.status}]")
            purchaseRepository.update(updateRequest.id, updateRequest.status).map(_ => Created)
          }
        )
  }

  def save(): Action[JsValue] = userAction.andThen(PermissionCheckAction).async(parse.json) {
    userRequest: UserRequest[JsValue] =>
      userRequest.request.body.validate[PurchaseRequest]
        .fold(jsonErrors(),
          items => {
            logger
              .info(s"Start updating purchases: new: [${items.newItems}], delete: [${items.idsForDelete}]")
            userRequest.user.flatMap(userOpt =>
              processPurchaseRequest(items, userOpt.getOrElse(throw new RuntimeException("System error")))
              .map(_ => Created))
          })
  }

  private def processPurchaseRequest(items: PurchaseRequest, user: User) = {
    val purchases = items.newItems.map {
      item => Purchase(0, item.name, item.status, item.createdAt.getOrElse(LocalDateTime.now()), Some(user.id))
    }
    Future.sequence(List(
      purchaseRepository.batchInsert(purchases),
      purchaseRepository.batchDelete(items.idsForDelete))
    )
  }
}
