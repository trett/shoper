package controllers.helpers

import play.components.CryptoComponents

import java.time.LocalDateTime
import java.util.UUID
import scala.collection.mutable

case class Session(token: String, email: String, expiration: LocalDateTime)

object SessionDao {
  private val session = mutable.Map.empty[String, Session]

  def getSession(token: String): Option[Session] = session.get(token)

  def generateToken(email: String): String = {
    val token = s"$email-token-${UUID.randomUUID().toString}"
    session.put(token, Session(token, email, LocalDateTime.now().plusHours(6)))
    token
  }
}
