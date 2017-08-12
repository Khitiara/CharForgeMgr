package charforgemgr.ui

import charforgemgr.Encounter

import scalafx.geometry.Insets
import scalafx.scene.Node
import scalafx.scene.control.{Button, Label, Separator, TitledPane}
import scalafx.scene.layout.{HBox, Priority, VBox}
import scalafx.scene.paint.Color

object EncounterRender {
  def makeCell(enc: Encounter, run: () => Unit): Node = {
    val nameLbl = new Label(enc.name) {
      hgrow = Priority.Always
      maxWidth = Double.PositiveInfinity
      textFill = Color.White
    }
    val runBtn = new Button("Run") {
      onAction = { _ =>
        run()
      }
    }
    val top = new HBox {
      hgrow = Priority.Always
      children = Seq(nameLbl, new Separator(), runBtn)
    }

    val mobsList = new VBox {
      hgrow = Priority.Always
      children = enc.mobs.map {
        case (m, amt) =>
          new Label(s"${amt}x ${m.name}")
      }
    }

    new TitledPane {
      //      padding = Insets(5)
      margin = Insets(5)
      hgrow = Priority.Always
      maxWidth = Double.PositiveInfinity
      maxHeight = Double.PositiveInfinity
      collapsible = false
      text = enc.name
      graphic = runBtn
      content = mobsList
      //      style = "-fx-border-color: black;"
    }
  }
}
