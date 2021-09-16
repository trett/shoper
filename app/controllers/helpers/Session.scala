package controllers.helpers

import java.time.LocalDateTime
import java.util.UUID
import scala.collection.mutable

case class Session(token: String, login: String, expiration: LocalDateTime)

object SessionDao {

  // TODO put in config
  private val ExpirationHours = 7200

  private val session = mutable.Map.empty[String, Session]

  def getSession(token: String): Option[Session] = session.get(token)

  def generateToken(login: String): String = {
    val token = s"$login-token-${UUID.randomUUID().toString}"
    session.put(token, Session(token, login, LocalDateTime.now().plusHours(ExpirationHours)))
    token
  }
}
