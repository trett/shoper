package controllers

import controllers.helpers.DatabaseExecutionContext
import controllers.helpers.PasswordHelper
import controllers.helpers.SessionDao
import models.UserRepository
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.i18n.Lang
import play.api.mvc._

import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.Future

case class LoginForm(login: String, password: String)

@Singleton
class LoginController @Inject() (
    val controllerComponents: ControllerComponents,
    messagesAction: MessagesActionBuilder,
    userRepository: UserRepository,
    implicit val ec: DatabaseExecutionContext
) extends BaseController {

  val logger: Logger = Logger(getClass.getCanonicalName)

  val loginConstraints: Form[LoginForm] = Form(
    mapping(
      "login"    -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginForm.apply)(LoginForm.unapply)
  )

  def loginForm(): Action[AnyContent] = messagesAction { implicit messageRequest: MessagesRequest[AnyContent] =>
    Ok(views.html.login(loginConstraints))
  }

  def login(): Action[AnyContent] = Action.async { implicit request =>
    loginConstraints
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(
            BadRequest(
              views.html.login(formWithErrors)(
                request,
                messagesApi.preferred(Seq(Lang.defaultLang))
              )
            )
          ),
        userData => {
          isValidLogin(userData.login, userData.password) map (valid =>
            if (valid) {
              logger.info(s"Successfully login userForm with email: [${userData.login}]")
              val token = SessionDao.generateToken(userData.login)
              Redirect(routes.PurchaseController.index()).withSession(request.session + ("sessionToken" -> token))
            } else {
              Redirect(routes.LoginController.loginForm()).withNewSession
            }
          )
        }
      )
  }

  private def isValidLogin(login: String, password: String): Future[Boolean] = {
    logger.info(s"Trying to login userForm with login: [$login]")
    userRepository.findByLogin(login) map {
      case Some(user) => PasswordHelper.checkPassword(password, user.password)
      case _          => false
    }
  }
}
