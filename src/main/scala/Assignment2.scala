import Assignment1.LoggerActor.SaveFiles
import akka.actor.SupervisorStrategy.{Escalate, Restart, Resume, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}

object Assignment2 extends App {

  class LoggerSupervisor extends Actor with ActorLogging {
    import LoggerSupervisor._
    import Assignment1.LoggerActor._
    override def receive: Receive = {
      case InitialiseLogger =>
        val loggerActor = context.actorOf(Props[Assignment1.LoggerActor](), "loggerActor")
        context.become(supervisorHandler(loggerActor))
    }

    private def supervisorHandler(loggerActorRef: ActorRef): Receive = {
      case WarnLog(message) => loggerActorRef.forward(WarnMsg(message))
      case InfoLog(message) => loggerActorRef.forward(InfoMsg(message))
      case SaveFiles => loggerActorRef.forward(SaveFiles)
      case msg =>
        log.info(s"${msg.toString} not supported")
        loggerActorRef.forward(NormalException)
    }

    override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
      case _: NullPointerException => Resume
      case _: RuntimeException => Restart
      case _: Exception => Stop
      case _ => Escalate
    }
  }

  object LoggerSupervisor {
    case object InitialiseLogger
    case class WarnLog(message: String)
    case class InfoLog(message: String)
  }

  val system = ActorSystem("FaultTolerance")
  val supervisor = system.actorOf(Props[LoggerSupervisor](), "supervisor")
  import LoggerSupervisor._
  supervisor ! InitialiseLogger
  supervisor ! WarnLog("warn log from supervisor")
  supervisor ! InfoLog("info log from supervisor")
  supervisor ! "random message"
  supervisor ! SaveFiles
}
