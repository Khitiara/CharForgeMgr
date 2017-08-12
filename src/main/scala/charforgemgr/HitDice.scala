package charforgemgr

import play.api.libs.json.Json

case class HitDice(num: Int, size: Int) {
  def roll(conMod: Int): Int = (0 until num).map(_ => (Math.random() * size).toInt).sum + num * (1 + conMod)
}

trait HitDiceFormat {
  implicit val hdFormat = Json.format[HitDice]
}

object HitDice extends HitDiceFormat
