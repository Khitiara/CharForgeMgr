package charforgemgr

import play.api.libs.json.{Format, Json, Reads, Writes}

case class Monster(xp: Int, name: String, initBonus: Int, hd: HitDice, conMod: Int) {
  def rollHd: Int = hd.roll(conMod)
}

trait MonsterJson {
  implicit val monsterFormat: Format[Monster] = Json.format[Monster]
  implicit val monsterDBFormat: Format[Map[String, Monster]] = Format[Map[String, Monster]](Reads.mapReads[Monster], Writes.mapWrites[Monster])
}

object Monster extends MonsterJson
