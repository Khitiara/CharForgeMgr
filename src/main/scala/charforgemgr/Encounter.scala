package charforgemgr

import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.collection.immutable.TreeMap
import scala.language.implicitConversions
import scalafx.collections.ObservableMap

object InitiativeRoller {
  def rollInit(bonus: Int): Int = (Math.random() * 20).toInt + 1 + bonus
}

sealed trait EncounterPartyMember {
  def init: Int

  def char: CachedCharacter

  def makeInst: PlayerInstance = new PlayerInstance(char, char.maxHP)
}

case class ManualInit(initVal: Int, char: CachedCharacter) extends EncounterPartyMember {
  def init: Int = initVal
}

case class AutoInit(char: CachedCharacter) extends EncounterPartyMember {
  override def init: Int = InitiativeRoller.rollInit(char.init)
}

case class Encounter(name: String, mobs: Map[Monster, Int]) {

  def start(party: Seq[EncounterPartyMember]): RunnableEncounter = {
    val mobs = collateMobInit
    val players = rollPlayersInit(party)
    val init = players.map { case (i, o) => (i, Left(o)) } ++ mobs.map { case (i, o) => (i, Right(o)) }
    RunnableEncounter(collateInits(init.toList))
  }

  private def makeMob(m: Monster): MonsterInstance = {
    val mhp = m.rollHd
    new MonsterInstance(m, mhp, mhp)
  }

  private def rollMobInit(m: Monster, amt: Int): (Int, Seq[MonsterInstance]) = (InitiativeRoller.rollInit(m.initBonus), (0 until amt).map(_ => makeMob(m)))

  private def collateMobInit: Seq[(Int, MonsterInstance)] = for {
    (mob, amt) <- mobs.toSeq
    (init, insts) = rollMobInit(mob, amt)
    inst <- insts
  } yield (init, inst)

  private def rollPlayersInit(p: Seq[EncounterPartyMember]): Seq[(Int, PlayerInstance)] = p.map(a => (a.init, a.makeInst))

  private def collateInits(vals: List[(Int, Either[PlayerInstance, MonsterInstance])], out: TreeMap[Int, Seq[Either[PlayerInstance, MonsterInstance]]] = TreeMap.empty(Ordering[Int].reverse)): Map[Int, Seq[Either[PlayerInstance, MonsterInstance]]] = vals match {
    case (i, e) :: xs => collateInits(xs, out.updated(i, out.getOrElse(i, Seq.empty) :+ e))
    case Nil => out
  }
}

trait EncounterFormat extends MonsterJson {
  def encFormat(implicit mobLookup: ObservableMap[String, Monster]): Format[Encounter] = {
    implicit val mapFmt: Format[Map[Monster, Int]] = Format[Map[Monster, Int]](Reads.mapReads[Monster, Int](mobLookup.get(_) match {
      case Some(m) => JsSuccess(m)
      case None => JsError()
    }), Writes.mapWrites[Int].contramap(_.map { case (a, b) => (a.name, b) }))
    Json.format[Encounter]
  }
}

object Encounter extends EncounterFormat

case class RunnableEncounter(initiative: Map[Int, Seq[Either[PlayerInstance, MonsterInstance]]])

sealed trait CombatInstance {
  var curHp: Int

  def name: String

  def maxHp: Int
}

class PlayerInstance(player: CachedCharacter, var curHp: Int) extends CombatInstance {
  def name: String = player.name

  def maxHp: Int = player.maxHP
}

class MonsterInstance(monster: Monster, var curHp: Int, val maxHp: Int) extends CombatInstance {
  def name: String = monster.name
}
