package controllers

import controllers.helpers.DatabaseExecutionContext
import controllers.helpers.PasswordHelper
import controllers.helpers.UserAction
import controllers.helpers.UserRequest
import controllers.helpers.RequestHelper.PermissionCheckAction
import models.User
import models.UserRepository
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.i18n.Lang
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.mvc._

import javax.inject.Inject
import javax.inject.Singleton
import scala.concurrent.Future
import play.twirl.api.HtmlFormat

case class UserProfile(email: Option[String], name: Option[String])
case class CreateUser(login: String, password: String, name: Option[String], email: Option[String])

@Singleton
class UserController @Inject() (
    val controllerComponents: ControllerComponents,
    val userAction: UserAction,
    val userRepository: UserRepository,
    implicit val ec: DatabaseExecutionContext
) extends BaseController
    with I18nSupport {

  val logger: Logger = Logger(getClass.getCanonicalName)

  implicit val ProfileWrites: Writes[User] = Json.writes[User]

  val userProfileConstraints: Form[UserProfile] = Form(
    mapping(
      "email" -> optional(email),
      "name"  -> optional(text)
    )(UserProfile.apply)(UserProfile.unapply)
  )

  val createUserConstraints: Form[CreateUser] = Form(
    mapping(
      "login"    -> nonEmptyText,
      "password" -> nonEmptyText,
      "email"    -> optional(email),
      "name"     -> optional(text)
    )(CreateUser.apply)(CreateUser.unapply)
  )

  def users(): Action[AnyContent] =
    userAction.andThen(PermissionCheckAction).async { userRequest: UserRequest[AnyContent] =>
      userRequest.user.flatMap(_ => userRepository.list() map (u => Ok(views.html.users(u))))
    }

  def createUserForm(): Action[AnyContent] =
    userAction.andThen(PermissionCheckAction).async { implicit userRequest: UserRequest[AnyContent] =>
      userRequest.user map (_ => Ok(showCreateForm(createUserConstraints, userRequest)))
    }

  def profileForm(): Action[AnyContent] =
    userAction.andThen(PermissionCheckAction).async { implicit userRequest: UserRequest[AnyContent] =>
      userRequest.user map (userOpt => Ok(showProfileForm(userProfileConstraints, userOpt.get, userRequest)))
    }

  def save(): Action[AnyContent] =
    userAction.andThen(PermissionCheckAction).async { implicit userRequest: UserRequest[AnyContent] =>
      createUserConstraints
        .bindFromRequest()
        .fold(
          errors => Future.successful(BadRequest(showCreateForm(errors, userRequest))),
          saveUser(_)
        )
    }

  def update(): Action[AnyContent] =
    userAction.andThen(PermissionCheckAction).async { implicit userRequest: UserRequest[AnyContent] =>
      userProfileConstraints
        .bindFromRequest()
        .fold(
          errors => userRequest.user map (userOpt => BadRequest(showProfileForm(errors, userOpt.get, userRequest))),
          requestData => userRequest.user.flatMap(userOpt => { updateProfile(userOpt.get, requestData) })
        )
    }

  private def showCreateForm(
      form: Form[CreateUser],
      userRequest: UserRequest[AnyContent]
  ): HtmlFormat.Appendable =
    views.html.createUserForm(form)(
      userRequest.request,
      messagesApi.preferred(Seq(Lang.defaultLang))
    )

  private def showProfileForm(
      form: Form[UserProfile],
      user: User,
      userRequest: UserRequest[AnyContent]
  ): HtmlFormat.Appendable =
    views.html.profileForm(form, UserProfile(user.email, user.name))(
      userRequest.request,
      messagesApi.preferred(Seq(Lang.defaultLang))
    )

  private def updateProfile(user: User, userProfile: UserProfile): Future[Result] = {
    logger.info(s"Updating user with login: [${user.login}], new name: [${user.name}]")
    userRepository.update(user.id, userProfile.email, userProfile.name) map (_ => REDIRECT_TO_USERS)
  }

  private def saveUser(createUser: CreateUser): Future[Result] = {
    logger.info(s"Saving user with login: [${createUser.login}]]")
    val pass    = PasswordHelper.hashPassword(createUser.password)
    val newUser = User(0, createUser.login, createUser.email, pass, createUser.name)
    userRepository.save(newUser) map (_ => REDIRECT_TO_USERS)
  }

  private val REDIRECT_TO_USERS = Redirect(routes.UserController.users())
}
