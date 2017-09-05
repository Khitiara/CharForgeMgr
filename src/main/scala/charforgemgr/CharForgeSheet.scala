package charforgemgr

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class CharForgeSheet(app: Sheets, val id: String) extends Helpers {
//  def strScore: Future[Int] = readInt("Str")
//  def strMod: Future[Int] = readInt("StrMod")
//  def strSave: Future[Int] = readInt("Final!$Y$81")
//  def dexScore: Future[Int] = readInt("Dex")
//  def dexMod: Future[Int] = readInt("DexMod")
//  def dexSave: Future[Int] = readInt("Final!$Z$81")
//  def conScore: Future[Int] = readInt("Con")
//  def conMod: Future[Int] = readInt("ConMod")
//  def conSave: Future[Int] = readInt("Final!$AA$81")
//  def intScore: Future[Int] = readInt("Int")
//  def intMod: Future[Int] = readInt("IntMod")
//  def intSave: Future[Int] = readInt("Final!$AB$81")
//  def wisScore: Future[Int] = readInt("Wis")
//  def wisMod: Future[Int] = readInt("WisMod")
//  def wisSave: Future[Int] = readInt("Final!$AC$81")
//  def chaScore: Future[Int] = readInt("Cha")
//  def chaMod: Future[Int] = readInt("ChaMod")
//  def chaSave: Future[Int] = readInt("Final!$AD$81")

  def maxHp: Future[Int] = readInt("HP")

  def ac: Future[Int] = readInt("ArmorClass")

  def init: Future[Int] = readInt("Final!A19")

  def pp: Future[Int] = readInt("Start!$BC$63")

  def prof: Future[Int] = readInt("Prof")

  def name: Future[String] = readSingle[String]("Start!$BN$16")

  def lvl: Future[Int] = readInt("Lvl")

  private def readInt(r: String) = readSingle[java.math.BigDecimal](r).map(_.intValue())

  private def readSingle[A](range: String) = for {
    values <- read(range)
  } yield values.get(0).get(0).asInstanceOf[A]

  private def read(range: String) = for {
    values <- app.spreadsheets().values().get(id, range).setValueRenderOption("UNFORMATTED_VALUE").run()
  } yield values.getValues

  def xpAmount: Future[Float] = readFloat("Start!$H$16")

  def setXp(xp: Float): Future[Float] = for {
    _ <- set("Start!$H$16", xp.asInstanceOf[AnyRef])
    newXp <- xpAmount
  } yield newXp

  def addXp(xp: Float): Future[Float] = for {
    curXp <- xpAmount
    newXp <- setXp(curXp + xp)
  } yield newXp

  private def readFloat(r: String) = readSingle[java.math.BigDecimal](r).map(_.floatValue())

  private def set(range: String, value: AnyRef) = {
    app.spreadsheets().values().update(id, range, new ValueRange().setValues(List(List(value).asJava).asJava)).setValueInputOption("RAW").run().map(_ -> Unit)
  }
}

case class CachedCharacter(sheet: CharForgeSheet, name: String, maxHP: Int, ac: Int, init: Int, lvl: Int) {
  def addXp(xp: Float): Future[CachedCharacter] = for {
    _ <- sheet.addXp(xp)
    newLvl <- sheet.lvl
  } yield copy(lvl = newLvl)
}

object CachedCharacter {
  def apply(sheet: CharForgeSheet): Future[CachedCharacter] = for {
    name <- sheet.name
    maxHP <- sheet.maxHp
    ac <- sheet.ac
    init <- sheet.init
    lvl <- sheet.lvl
  } yield CachedCharacter(sheet, name, maxHP, ac, init, lvl)
}
