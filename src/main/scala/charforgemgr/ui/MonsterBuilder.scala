package charforgemgr.ui

import charforgemgr.{HitDice, Monster}

import scalafx.beans.binding.Bindings
import scalafx.beans.property.ObjectProperty
import scalafx.geometry.{HPos, Insets}
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout.{GridPane, HBox, Priority}
import scalafx.stage.Stage
import scalafx.util.converter.IntStringConverter

object MonsterBuilder {
  def makeMonster(): Option[Monster] = {
    val nameBox = new TextField
    GridPane.setConstraints(nameBox, 1, 0)

    val nameLbl = new Label("Name")
    GridPane.setConstraints(nameLbl, 0, 0)

    val hdNum = new Spinner[Int](1, 500, 1)
    val hdSize = new ComboBox[Int](Seq(4, 6, 8, 10, 12, 20))
    val conMod = new Spinner[Int](-5, 10, 0)
    val hdLblText = Bindings.createStringBinding(() => s" + (${hdNum.value.value} * ")
    val hd = new HBox {
      children = Seq(hdNum, new Label("d"), hdSize, new Label {
        text <== hdLblText
      }, conMod, new Label(")"))
    }
    GridPane.setConstraints(hd, 1, 1)

    val hdLbl = new Label("Hit Points")
    GridPane.setConstraints(hdLbl, 0, 1)

    val initMod = new Spinner[Int](-5, 10, 0)
    GridPane.setConstraints(initMod, 1, 2)
    val initLbl = new Label("Initiative")
    GridPane.setConstraints(initLbl, 0, 2)

    val xpValue = new TextField {
      textFormatter = new TextFormatter[Int](new IntStringConverter)
    }
    GridPane.setConstraints(xpValue, 1, 3)
    val xpLbl = new Label("XP Value")
    GridPane.setConstraints(xpLbl, 0, 3)


    val resultProp = new ObjectProperty[Monster]


    object Recurse {
      val stage: Stage = new Stage {
        scene = new Scene(600, 450) {
          stylesheets = List(getClass.getResource("styles.css").toExternalForm)
          val createBtn = new Button {
            text = "Create"
            onAction = { _ =>
              resultProp.value = new Monster(xpValue.textFormatter.value.getValue.asInstanceOf[Int], nameBox.text.value, initMod.value.value, HitDice(hdNum.value.value, hdSize.value.value), conMod.value.value)
              stage.close()
            }
          }
          GridPane.setConstraints(createBtn, 1, 4)
          GridPane.setHalignment(createBtn, HPos.Right)
          val ui = new GridPane {
            hgrow = Priority.Always
            vgrow = Priority.Always
            padding = Insets(10)
            hgap = 5
            vgap = 5
            children = Seq(nameBox, nameLbl, hd, hdLbl, initLbl, initMod, xpLbl, xpValue, createBtn)
          }
          root = ui
        }
      }

      def run(): Unit = {
        stage.showAndWait()
      }
    }
    Recurse.run()
    Option(resultProp.value)
  }
}
