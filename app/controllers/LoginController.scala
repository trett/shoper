package controllers

import controllers.helpers.{PasswordHelper, SessionDao}
import models.Tables.users
import models.User
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc._
import play.db.NamedDatabase
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LoginController @Inject()
(@NamedDatabase("shoper") protected val dbConfigProvider: DatabaseConfigProvider,
 val controllerComponents: ControllerComponents, messagesAction: MessagesActionBuilder)
(implicit ec: ExecutionContext)
  extends BaseController with HasDatabaseConfigProvider[PostgresProfile] {

  val logger: Logger = Logger("login")

  val userForm: Form[User] = Form(
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    )(User.apply)(User.unapply)
  )

  def loginForm(): Action[AnyContent] = messagesAction {
    implicit messageRequest: MessagesRequest[AnyContent] =>
      Ok(views.html.login(userForm))
  }

  def login(): Action[User] = Action.async(parse.form(userForm)) {
    implicit request =>
      val form = request.body
      isValidLogin(form.email, form.password).map(valid => if (valid) {
        val token = SessionDao.generateToken(form.email)
        Redirect(routes.FrontController.index()).withSession(request.session + ("sessionToken" -> token))
      } else {
        Redirect(routes.LoginController.loginForm()).withNewSession
      })
  }

  private def isValidLogin(email: String, password: String): Future[Boolean] = {
    logger.info(s"Trying to login user with email: [$email]")
    val userOpt = db.run(users.filter(user => user.email === email).result.headOption)
    userOpt.map {
      case Some(user) => PasswordHelper.checkPassword(password, user.password)
      case _ => false
    }
  }
}
