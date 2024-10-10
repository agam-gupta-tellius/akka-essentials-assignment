import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class BankManagementSpec extends TestKit(ActorSystem("bankSpec"))
  with ImplicitSender
  with Matchers
  with AnyWordSpecLike
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Bank Management" should {
    import Assignment3.BankManagement._
    val bank = system.actorOf(Props[Assignment3.BankManagement](), "bank")
    "open an account" in {
        bank ! OpenAccount
        expectMsg(AccountOpenSuccess)
    }

    "deposit money" in {
      bank ! Deposit(300)
      expectMsg(TransactionSuccess)
    }

    "throw failure on insufficient balance" in {
      bank ! Withdrawl(500)
      expectMsg(TransactionFailure)
    }

    "successfully withdraws money" in {
      bank ! Withdrawl(10)
      expectMsg(TransactionSuccess)
    }

    "check balance" in {
      bank ! CheckBalance
      val response = expectMsgType[String]
      assert(response == "Current balance is 290")
    }
  }
}
