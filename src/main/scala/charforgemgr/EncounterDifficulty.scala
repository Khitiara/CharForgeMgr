package charforgemgr

object EncounterDifficulty {

  def rate(party: CachedParty)(encounter: Encounter): EncounterRating = {
    val totXp = encounter.mobs.map { case (mob, amt) => mob.xp * amt }.sum
    val numMobs = encounter.mobs.values.sum

    val numChars = party.chars.size
    val xpPer = totXp / numChars

    val adjuster = getAdjustMultiplier(numChars, numMobs)
    val adjXp = totXp * adjuster

    EncounterRating(totXp, xpPer, adjXp, partyThreshold(party).rate(adjXp))
  }

  private def getAdjustMultiplier(nChars: Int, nMobs: Int): Float = {
    val multipliers: Array[Float] = Array(0.5f, 1f, 1.5f, 2f, 2.5f, 3f, 4f, 5f)
    var multiplier = 0
    for (thresh <- Seq(0, 1, 2, 6, 10, 14)) if (nMobs > thresh) multiplier += 1

    if (1 until 3 contains nChars) multiplier += 1
    else if (nChars >= 6) multiplier -= 1

    multipliers(multiplier)
  }

  private def partyThreshold(p: CachedParty) = p.chars.map(c => thresholdByLevel(c.lvl)).fold(XpThreshold(0, 0, 0, 0))(_ + _)

  private def thresholdByLevel = Map(
    1 -> XpThreshold(25, 50, 75, 100),
    2 -> XpThreshold(50, 100, 150, 200),
    3 -> XpThreshold(75, 150, 225, 400),
    4 -> XpThreshold(125, 250, 375, 500),
    5 -> XpThreshold(250, 500, 750, 1100),
    6 -> XpThreshold(300, 600, 900, 1400),
    7 -> XpThreshold(350, 750, 1100, 1700),
    8 -> XpThreshold(450, 900, 1400, 2100),
    9 -> XpThreshold(550, 1100, 1600, 2400),
    10 -> XpThreshold(600, 1200, 1900, 2800),
    11 -> XpThreshold(800, 1600, 2400, 3600),
    12 -> XpThreshold(1000, 2000, 3000, 4500),
    13 -> XpThreshold(1100, 2200, 3400, 5100),
    14 -> XpThreshold(1250, 2500, 3800, 5700),
    15 -> XpThreshold(1400, 2800, 4300, 6400),
    16 -> XpThreshold(1600, 3200, 4800, 7200),
    17 -> XpThreshold(2000, 3900, 5900, 8800),
    18 -> XpThreshold(2100, 4200, 6300, 9500),
    19 -> XpThreshold(2400, 4900, 7300, 10900),
    20 -> XpThreshold(2800, 5700, 8500, 12700)
  ).apply(_)

  sealed trait Difficulty

  case class EncounterRating(xp: Int, xpPerChar: Float, adjXp: Float, difficulty: Difficulty)

  private case class XpThreshold(easy: Int, med: Int, hard: Int, deadly: Int) {
    def rate(xp: Float): Difficulty =
      if (xp >= deadly) Deadly
      else if (xp >= hard) Hard
      else if (xp >= med) Medium
      else if (xp >= easy) Easy
      else Trivial

    def +(other: XpThreshold) = XpThreshold(easy + other.easy, med + other.med, hard + other.hard, deadly + other.deadly)
  }

  case object Trivial extends Difficulty

  case object Easy extends Difficulty

  case object Medium extends Difficulty

  case object Hard extends Difficulty

  case object Deadly extends Difficulty

}
