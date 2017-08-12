package charforgemgr

import com.google.api.client.googleapis.services.AbstractGoogleClientRequest

import scala.concurrent.{ExecutionContext, Future}

trait Helpers {
  implicit val ec = ExecutionContext.global

  implicit class RichClientRequest[A](self: AbstractGoogleClientRequest[A]) {
    def run() = Future {
      self.execute()
    }
  }

}
