package controllers

import controllers.helpers.RequestHelper.{process, futureProcess, showError, REDIRECT_TO_LOGIN}
import controllers.helpers.{DatabaseExecutionContext, PasswordHelper, UserAction, UserRequest}
import models.{User, UserRepository}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.libs.json.{Json, Writes}
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future


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

  val userConstraints: Form[User] = Form(
    mapping(
      "id" -> longNumber,
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "name" -> optional(text)
    )(User.apply)(User.unapply)
  )

  def users(): Action[AnyContent] = userAction.async {
    userRequest: UserRequest[AnyContent] => {
      userRequest.user.flatMap(userOption => {
        userOption.map(_ => {
          userRepository.list().map(u => Ok(views.html.users(u)))
        }).getOrElse(Future.successful(REDIRECT_TO_LOGIN))
      })
    }
  }

  def form(): Action[AnyContent] = userAction.async {
    implicit userRequest: UserRequest[AnyContent] => {
      userRequest.user.map(process {
        _ =>
          Ok(views.html.userForm(userConstraints)(userRequest, userRequest.request.messages))
      })
    }
  }

  def updateForm(): Action[AnyContent] = userAction.async {
    implicit userRequest: UserRequest[AnyContent] => {
      userRequest.user.map(process {
        user =>
          Ok(views.html.userUpdateForm(userConstraints, user.email, user.name)
          (userRequest, userRequest.request.messages))
        })
    }
  }

    def save(): Action[AnyContent] = userAction.async {
      implicit userRequest: UserRequest[AnyContent] => {
        userConstraints.bindFromRequest().fold(showError(userRequest, messagesApi),
          userData => {
            userRequest.user.flatMap( 
              futureProcess {
                user =>
                logger.info(s"Saving user with email: [${userData.email}], new name: [${userData.name}]")
                val newUser = User(0, userData.email, PasswordHelper.hashPassword(userData.password), userData.name)
                userRepository.save(newUser).map(_ => Redirect(routes.UserController.users()))
              })
          })
      }
    }


  def update(): Action[AnyContent] = userAction.async(userAction.parser) {
    implicit userRequest: UserRequest[AnyContent] => {
      userConstraints.bindFromRequest().fold(showError(userRequest, messagesApi),
        userData => {
          userRequest.user.flatMap(
            futureProcess {
              user =>
                logger.info(s"Updating user with email: [${userData.email}], new name: [${userData.name}]")
                // check email belong to user session
                if (user.email != userData.email) {
                  throw new RuntimeException("Error")
                }
                val updatedUser =
                  User(user.id, user.email,PasswordHelper.hashPassword(userData.password), userData.name)
                userRepository.update(updatedUser).map(_ => Redirect(routes.UserController.users()))
            })
        })
    }
  }
}
