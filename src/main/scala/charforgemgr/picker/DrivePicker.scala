package charforgemgr.picker

import java.awt.Desktop
import java.net.URI

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener
import org.scalatra.{Ok, ScalatraServlet}
import play.api.libs.json.{JsError, JsSuccess, Json}

import concurrent.ExecutionContext.Implicits._
import scala.concurrent.{Future, Promise}
import scala.io.{Codec, Source, StdIn}

object DrivePicker {
  private val done: Promise[Array[String]] = Promise[Array[String]]()

  class PickerServlet extends ScalatraServlet {
    get("/picker") {
      contentType = "text/html"
      Ok(Source.fromResource("charforgemgr/picker/picker.html")(Codec.UTF8).mkString)
    }
    post("/files") {
      println(params)
      Json.parse(params("result-input")).validate[Array[String]] match {
        case JsSuccess(files, _) => done.success(files)
        case JsError(_) => done.failure(new Exception("JSON Parse Error!"))
      }
      Ok("It is now safe to close this tab!")
    }
  }

  def run(): (() => Unit, Future[Array[String]]) = {
    val port = if (System.getenv("PORT") != null) System.getenv("PORT").toInt else 8080

    val server = new Server(port)
    val context = new WebAppContext()
    context setContextPath "/"
    context.setResourceBase("src/main/webapp")
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[PickerServlet], "/")

    server.setHandler(context)

    server.start()
    Desktop.getDesktop.browse(new URI("http://localhost:8080/picker"))
    (() => server.stop(), done.future.transform(a => {
      server.stop()
      a
    }).andThen { case _ => server.join() })
  }
}
