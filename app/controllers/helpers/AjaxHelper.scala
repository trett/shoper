package controllers.helpers

import play.api.mvc.Results.Forbidden
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

object AjaxHelper {

  def PermissionCheckAction(implicit ec: ExecutionContext): ActionFilter[UserRequest] = new ActionFilter[UserRequest] {
    def executionContext: ExecutionContext = ec

    def filter[A](input: UserRequest[A]): Future[Option[Result]] = {
      input.user.map(user => if (user.isEmpty) Some(Forbidden) else None)
    }
  }
}
