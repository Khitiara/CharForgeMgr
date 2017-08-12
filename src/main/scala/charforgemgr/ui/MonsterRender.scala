package charforgemgr.ui

import charforgemgr.Monster

import scalafx.geometry.Insets
import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

object MonsterRender {
  def makeCell(m: Monster): Node = {
    val nameLbl = new Label(m.name)
    val botLbl = new Label(s"XP: ${m.xp} | HP: ${m.hd.num}d${m.hd.size} + ${m.conMod * m.hd.num}")

    new VBox {
      margin = Insets(5)
      children.addAll(nameLbl, botLbl)
      padding = Insets(5)
      style = "-fx-border-color: black;"
    }
  }
}
