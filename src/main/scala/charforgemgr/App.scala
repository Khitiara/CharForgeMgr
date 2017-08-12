package charforgemgr

import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.io.StdIn
import scalafx.collections.ObservableMap

object App {
  def main(args: Array[String]): Unit = {
    val (service, _) = Authenticator.services()

    val characters = Array("17UOszS_KaS8uRr3M8RXRSzVF9gj1zQMzYE0TYE3m6A8", "1cwTBkAMkKdmP5HSl9vLIv2w0WxVxX_Y7fmOZufA_XI8", "1NdTbIUxw1mRYxS3v6b2KjMlTQN0ACzvkWfedWfo6ys8", "1gfEHgq1bmkTlBRNDkejsOruUPvt2cfSt_qqFHlXdpDA", "1gphmUgRf2xeTPiEgdCiS8Kb1Ow-W8Yl0jVYmQeVuVsQ")

    val charSheets = characters.map(new CharForgeSheet(service, _))
    val party = Party("testing", charSheets)

    val wererat = Monster(450, "Wererat", 2, HitDice(6, 8), 1)
    val giantRat = Monster(25, "Giant Rat", 2, HitDice(2, 6), 0)

    val mobDb = Map(
      wererat.name -> wererat,
      giantRat.name -> giantRat
    )

    import Monster._
    println(Json.toJson(mobDb).toString())


    val enc = Encounter("Rats", Map(
      wererat -> 2,
      giantRat -> 3
    ))

    implicit val encFmt = Encounter.encFormat(ObservableMap(mobDb.toSeq))
    println(Json.toJson(enc).toString())

    implicit val ec: ExecutionContextExecutor = ExecutionContext.global

    println("Running...")

    party.preLoad.map(EncounterDifficulty.rate(_)(enc)).onComplete(println(_))

    //    party.preLoad.foreach { p1 =>
    //      val encParty = p1.setupInit(s => AutoInit(s))
    //
    //      val e = enc.start(encParty)
    //      e.initiative.flatMap {
    //        case (i, s) => s.map {
    //          case Left(p) => (i, s"${p.player.name} with HP ${p.curHp}")
    //          case Right(m) => (i, s"${m.monster.name} with HP ${m.curHp}")
    //        }
    //      }.foreach {
    //        case (init, what) => println(s"$what goes on initiative count $init")
    //      }
    //
    //      val mobLookup = Map(
    //        "Wererat" -> wererat,
    //        "Giant Rat" -> giantRat
    //      )
    //      implicit val encFmt = Encounter.encFormat(mobLookup)
    //      println(Json.toJson(enc).toString())
    //
    //      implicit val partyFmt = Party.format(service)
    //      println(Json.toJson(party).toString())
    //    }
    StdIn.readLine()
  }
}
