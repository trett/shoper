package controllers.helpers

import controllers.routes
import models.User
import play.api.data.Form
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.Results.{BadRequest, Forbidden, Redirect}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

object RequestHelper {

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
      userOption.map(user => userToResult(user))
        .getOrElse(Redirect(routes.LoginController.loginForm()))
    }
  }
}
