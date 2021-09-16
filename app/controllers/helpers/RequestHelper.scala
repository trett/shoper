package controllers.helpers

import play.api.libs.json.JsError
import play.api.libs.json.JsPath
import play.api.libs.json.Json
import play.api.libs.json.JsonValidationError
import play.api.mvc.Results.BadRequest
import play.api.mvc.Results.Forbidden
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object RequestHelper {

  def PermissionCheckAction(implicit ec: ExecutionContext): ActionFilter[UserRequest] =
    new ActionFilter[UserRequest] {
      def executionContext: ExecutionContext = ec

      def filter[A](input: UserRequest[A]): Future[Option[Result]] =
        input.user.map { user =>
          if (user.isEmpty) Some(Forbidden) else None
        }
    }

  def jsonErrors()
      : collection.Seq[(JsPath, collection.Seq[JsonValidationError])] => Future[Result] = {
    errors: scala.collection.Seq[
      (JsPath, scala.collection.Seq[JsonValidationError])
    ] =>
      Future.successful(
        BadRequest(Json.obj("message" -> JsError.toJson(errors)))
      )
  }
}
