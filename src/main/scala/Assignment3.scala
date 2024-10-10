import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

object Assignment3 extends App {
  class BankManagement extends Actor with ActorLogging {
    import BankManagement._
    override def receive: Receive = {
      case OpenAccount => 
        sender() ! AccountOpenSuccess
        context.become(accountHandler(0))
      case msg =>
        log.info(s"${msg.toString} is not supported")
        throw new RuntimeException("operation not supported")
    }

    private def accountHandler(balance: Int): Receive = {
      case Deposit(amt) =>
        if(amt <= 0)
          log.info(s"Amount can't be negative")
          sender() ! TransactionFailure
        else
          sender() ! TransactionSuccess
          context.become(accountHandler(balance + amt))
      case Withdrawl(amt) =>
        if(amt > balance)
          log.info(s"Balance is insufficient")
          sender() ! TransactionFailure
        else
          sender() ! TransactionSuccess
          context.become(accountHandler(balance-amt))
      case CheckBalance =>
        sender() ! s"Current balance is $balance"
        log.info(s"Current balance is $balance")
    }
  }

  object BankManagement {
    case object OpenAccount
    case object CheckBalance
    case class Deposit(amt: Int)
    case class Withdrawl(amt: Int)
    case object TransactionSuccess
    case object TransactionFailure
    case object AccountOpenSuccess
  }

  val system = ActorSystem("bankManagement")
  val bank = system.actorOf(Props[BankManagement](), "bank")
  import BankManagement._

  bank ! CheckBalance
  bank ! OpenAccount
  bank ! Deposit(400)
  bank ! CheckBalance
  bank ! Withdrawl(500)
  bank ! Withdrawl(100)
  bank ! CheckBalance
}
