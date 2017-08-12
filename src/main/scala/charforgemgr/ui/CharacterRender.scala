package charforgemgr.ui

import charforgemgr.CachedCharacter

import scalafx.geometry.Insets
import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

object CharacterRender {
  def makeCell(c: CachedCharacter): Node = {
    val nameLbl = new Label(c.name)
    val botLbl = new Label(s"Level: ${c.lvl} | AC: ${c.ac} | HP: ${c.maxHP}")

    new VBox {
      margin = Insets(5)
      children.addAll(nameLbl, botLbl)
      padding = Insets(5)
      style = "-fx-border-color: black;"
    }
  }
}


