package charforgemgr

import com.google.api.services.sheets.v4.Sheets
import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

case class Party(name: String, chars: Seq[CharForgeSheet]) {
  def preLoad: Future[CachedParty] = for {
    c <- Future.sequence(chars.map(CachedCharacter.apply))
  } yield CachedParty(name, c)
}

case class CachedParty(name: String, chars: Seq[CachedCharacter]) {
  def setupInit(f: CachedCharacter => EncounterPartyMember): Seq[EncounterPartyMember] = chars.map(f)

  def distributeXP(amt: Float): Future[CachedParty] = for {
    updated <- Future.sequence(chars.map(_.addXp(amt / chars.size)))
  } yield copy(chars = updated)
}

trait PartyFormat {
  private def reads(service: Sheets): Reads[Party] =
    ((__ \ "name").read[String] and
      (__ \ "members").read[Seq[String]].map(_.map(new CharForgeSheet(service, _)))
      ) (Party(_, _))

  private def writes: Writes[Party] = ((__ \ "name").write[String] and (__ \ "members").write[Seq[String]].contramap[Seq[CharForgeSheet]](_.map(_.id))) (unlift((p: Party) => Party.unapply(p)))

  implicit def format(implicit service: Sheets): Format[Party] = Format[Party](reads(service), writes)
}

object Party extends PartyFormat
