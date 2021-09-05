package controllers.helpers

import controllers.routes
import models.User
import play.api.data.Form
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.{JsError, JsPath, Json, JsonValidationError}
import play.api.mvc.Results.{BadRequest, Forbidden, Redirect}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

object RequestHelper {

  val REDIRECT_TO_LOGIN: Result = Redirect(routes.LoginController.loginForm())

  def PermissionCheckAction(implicit ec: ExecutionContext): ActionFilter[UserRequest] = new ActionFilter[UserRequest] {
    def executionContext: ExecutionContext = ec

    def filter[A](input: UserRequest[A]): Future[Option[Result]] = {
      input.user.map(user => if (user.isEmpty) Some(Forbidden) else None)
    }
  }

  def showError(userRequest: UserRequest[AnyContent], messagesApi: MessagesApi): Form[User] => Future[Result] = {
    formWithErrors: Form[User] => {
      Future.successful(
        BadRequest(views.html.userForm(formWithErrors)
        (userRequest.request, messagesApi.preferred(Seq(Lang.defaultLang))))
      )
    }
  }

  def process(userToResult: User => Result): Option[User] => Result = {
    userOption: Option[User] => {
      userOption.map(user => userToResult(user)) getOrElse REDIRECT_TO_LOGIN
    }
  }

  def jsonErrors(): collection.Seq[(JsPath, collection.Seq[JsonValidationError])] => Future[Result] = {
    errors: scala.collection.Seq[(JsPath, scala.collection.Seq[JsonValidationError])] =>
      Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
  }
}
