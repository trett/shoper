package controllers.helpers

import models.Tables.users
import models.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc._
import play.db.NamedDatabase
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserRequest[A](val user: Option[Future[User]], val request: Request[A]) extends WrappedRequest[A](request)

class UserAction @Inject()
(val parser: BodyParsers.Default,
 @NamedDatabase("shoper") protected val dbConfigProvider: DatabaseConfigProvider)
(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[UserRequest, AnyContent]
    with ActionTransformer[Request, UserRequest]
    with HasDatabaseConfigProvider[PostgresProfile] {

  def transform[A](request: Request[A]) = Future.successful {
    val user = extractUser(request)
    new UserRequest(user, request)
  }

  private def extractUser(req: RequestHeader): Option[Future[User]] = {
    val sessionTokenOpt = req.session.get("sessionToken")
    sessionTokenOpt
      .flatMap(token => SessionDao.getSession(token))
      .filter(_.expiration.isAfter(LocalDateTime.now()))
      .map(_.email)
      .map(email => db.run(users.filter(_.email === email).result.head))
  }
}
