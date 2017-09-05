import javax.servlet.ServletContext

import charforgemgr.picker.DrivePicker
import org.scalatra.LifeCycle

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context mount (new DrivePicker.PickerServlet, "/*")
  }
}
