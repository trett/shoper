package controllers.helpers

import akka.actor.ActorSystem
import com.google.inject.ImplementedBy
import play.api.libs.concurrent.CustomExecutionContext

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@ImplementedBy(classOf[DatabaseExecutionContextImpl])
trait DatabaseExecutionContext extends ExecutionContext

@Singleton
class DatabaseExecutionContextImpl @Inject() (system: ActorSystem)
    extends CustomExecutionContext(system, "db.executor")
    with DatabaseExecutionContext
