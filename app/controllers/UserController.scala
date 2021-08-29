package controllers

import controllers.helpers.AjaxHelper.PermissionCheckAction
import controllers.helpers.{DatabaseExecutionContext, UserAction, UserRequest}
import models.{User, UserRepository}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, optional, text}
import play.api.i18n.{I18nSupport, Lang}
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

case class UserForm(password: String, name: Option[String])

@Singleton
class UserController @Inject()
(val controllerComponents: ControllerComponents,
 val userAction: UserAction,
 val userRepository: UserRepository,
 implicit val ec: DatabaseExecutionContext)
  extends BaseController
    with I18nSupport {

  val logger: Logger = Logger(getClass.getCanonicalName)

  implicit val ProfileWrites: Writes[User] = Json.writes[User]

  val userConstraints: Form[UserForm] = Form(
    mapping(
      "password" -> nonEmptyText,
      "name" -> optional(text)
    )(UserForm.apply)(UserForm.unapply)
  )

  def page(): Action[AnyContent] = userAction.async {
    implicit userRequest: UserRequest[AnyContent] => {
      userRequest.user.map(userOption => {
        userOption.map(_ => {
          Ok(views.html.user(userConstraints)(userRequest, userRequest.request.messages))
        }).getOrElse(Redirect(routes.LoginController.loginForm()))
      })
    }
  }

  def get: Action[AnyContent] = userAction.andThen(PermissionCheckAction).async(userAction.parser) {
    userRequest: UserRequest[AnyContent] => {
      userRequest.user.flatMap(userOption => {
        userOption.map(user => {
          logger.info(s"Loading user with email: [${user.email}]")
          userRepository.findByEmail(user.email).map {
            // filter password
            case Some(value) => Ok(Json.toJson(User(value.email, "", value.name)))
            case None => BadRequest
          }
        }).getOrElse(Future {
          Redirect(routes.LoginController.loginForm())
        })
      })
    }
  }

  def update(): Action[AnyContent] = userAction.async(userAction.parser) {
    implicit userRequest: UserRequest[AnyContent] => {
      userConstraints.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(
            BadRequest(views.html.user(formWithErrors)(userRequest.request, messagesApi.preferred(Seq(Lang.defaultLang))))
          )
        },
        userData => {
          userRequest.user.map(userOption => {
            userOption.map(user => {
              logger.info(s"Updating user with email: [${user.email}], new name: [${user.name}]")
              userRepository.updatePassword(user.email, userData.password)
              logger.info(s"End of updating user with email: [${user.email}]")
              Redirect(routes.UserController.page())
            }).getOrElse(Redirect(routes.LoginController.loginForm()))
          })
        }
      )
    }
  }
}
