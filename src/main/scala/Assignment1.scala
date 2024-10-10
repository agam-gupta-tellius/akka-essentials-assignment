import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

import java.io.{File, FileWriter}

object Assignment1 extends App {

  class LoggerActor extends Actor with ActorLogging {
    val initialInfoFile = new File("infoFile.log")
    val initialWarnFile = new File("warnFile.log")

    import LoggerActor._
    override def receive: Receive = logHandler(new FileWriter(initialInfoFile, true), new FileWriter(initialWarnFile, true), 1)

    private def logHandler(infoFileWriter: FileWriter, warnFileWriter: FileWriter, fileSuffix: Int): Receive = {
      case WarnMsg(message) =>
        warnFileWriter.write(s"$message\n")

      case InfoMsg(message) =>
        infoFileWriter.write(s"$message\n")

      case SaveFiles =>
        warnFileWriter.flush()
        infoFileWriter.flush()
        warnFileWriter.close()
        infoFileWriter.close()

        val newInfoFile = new File(s"infoFile-$fileSuffix.log")
        val newWarnFile = new File(s"warnFile-$fileSuffix.log")
        context.become(logHandler(new FileWriter(newInfoFile), new FileWriter(newWarnFile), fileSuffix+1))

      case NormalException => throw new NullPointerException("null-pointer-exception")
      case PriorityException => throw new RuntimeException("runtime-exception")
      case _ => throw new Exception("other-exceptions")
    }

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      log.info(s"logger restarting due to ${reason.getMessage}")
    }
  }

  object LoggerActor {
    case class WarnMsg(message: String)
    case class InfoMsg(message: String)
    case object SaveFiles
    case object NormalException
    case object PriorityException
  }

  val system = ActorSystem("loggingDemo")
  val loggerActor = system.actorOf(Props(new LoggerActor), "loggerActor")
  loggerActor ! LoggerActor.InfoMsg("info message")
  loggerActor ! LoggerActor.WarnMsg("warn message")
  loggerActor ! LoggerActor.InfoMsg("info message")
  loggerActor ! LoggerActor.WarnMsg("warn message")
  loggerActor ! LoggerActor.InfoMsg("info message")
  loggerActor ! LoggerActor.WarnMsg("warn message")
  loggerActor ! LoggerActor.SaveFiles
  loggerActor ! LoggerActor.InfoMsg("new file info msg")
  loggerActor ! LoggerActor.WarnMsg("new file warn msg")
  loggerActor ! LoggerActor.SaveFiles
}
