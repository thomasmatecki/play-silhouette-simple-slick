package models

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import org.specs2.mutable._
import play.api.Application
import play.api.test.WithApplicationLoader

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

class PasswordDAOSpec @Inject()(implicit ec: ExecutionContext) extends Specification {

  val cache = Application.instanceCache[PasswordDAO]

  "PasswordDAO" should {

    val providerKey: String = System.currentTimeMillis.toString
    val loginInfo = LoginInfo("Test", providerKey)
    val passwordInfo = PasswordInfo("TestHasher", "SecretPassword", None)
    val loader = new WithApplicationLoader() {}

    val dao: PasswordDAO = cache(loader.app)

    "insert a record into the database, then retrieve the same record" in {

      Await.result({
        dao.add(loginInfo, passwordInfo) andThen {
          case Success(_) => dao.find(loginInfo)
          case Failure(_) => None
        }
      }, Duration.Inf) should be(passwordInfo)
    }

    "update a record into the database, then retrieve the same record" in {

      val newPasswordInfo = passwordInfo.copy(password = "A different Secret Password")

      Await.result({
        dao.save(loginInfo, newPasswordInfo) andThen {
          case Success(_) => dao.find(loginInfo)
          case Failure(_) => None
        }
      }, Duration.Inf) should be(newPasswordInfo)

    }

    "remove a record from the database, then fail to find it" in {

      val res = Await.result({
        dao.remove(loginInfo) andThen {
          case Success(_) => dao.find(loginInfo)
          case Failure(_) => None
        }
      }, Duration.Inf)

      res should_==()
    }

  }
}
