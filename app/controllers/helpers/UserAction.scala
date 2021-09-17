package controllers.helpers

import models.User
import models.UserRepository
import play.api.mvc._

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class UserRequest[A](val user: Future[Option[User]], val request: Request[A]) extends WrappedRequest[A](request)

class UserAction @Inject() (
    val parser: BodyParsers.Default,
    userRepository: UserRepository,
    implicit val ec: DatabaseExecutionContext
) extends ActionBuilder[UserRequest, AnyContent]
    with ActionTransformer[Request, UserRequest] {

  def transform[A](request: Request[A]) = Future.successful {
    val user = extractUser(request)
    new UserRequest(user, request)
  }

  private def extractUser(req: RequestHeader): Future[Option[User]] = {
    val sessionTokenOpt = req.session.get("sessionToken")
    sessionTokenOpt
      .flatMap(token => SessionDao.getSession(token))
      .filter(_.expiration.isAfter(LocalDateTime.now()))
      .map(_.login)
      .map(login => userRepository.findByLogin(login))
      .getOrElse(Future.successful(Option.empty[User]))
  }

  override protected def executionContext: ExecutionContext = ec
}
