package charforgemgr.ui

import charforgemgr._

import scala.concurrent.Future
import scalafx.Includes._
import scalafx.beans.binding.Bindings
import scalafx.beans.property.{BooleanProperty, IntegerProperty}
import scalafx.geometry.Insets
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.scene.paint.Color
import scalafx.scene.{Node, Scene}
import scalafx.stage.{Modality, Stage}

object EncounterDialog {
  def runEncounter(p: CachedParty, enc: Encounter): Future[CachedParty] = {
    val runEnc = getRunnableEncounter(p, enc)
    val rating = EncounterDifficulty.rate(p)(enc)

    val addXp = BooleanProperty(false)

    val initValues = runEnc.initiative.keys.toArray
    val currentInit = IntegerProperty(initValues.max)

    def makeInitiativeEntries(init: Int): Seq[Node] = {
      runEnc.initiative(init).map { item =>
        val initLbl = new Label(init.toString) {
          maxWidth = 30
          hgrow = Priority.Always
        }
        val inst = item.fold[CombatInstance](identity, identity)
        val nameLbl = new Label(inst.name) {
          maxWidth = Double.PositiveInfinity
          hgrow = Priority.Always
        }

        val hpBox = new Spinner[Int](0, inst.maxHp, inst.curHp) {
          hgrow = Priority.Always
          value.onChange { (_, _, v) =>
            inst.curHp = v
          }
        }
        new HBox {
          maxWidth = Double.PositiveInfinity
          hgrow = Priority.Always
          children = Seq(initLbl, new Separator(), nameLbl, new Separator(), hpBox)
          background <== when(currentInit === init)
            .choose(new Background(Array(new BackgroundFill(Color.Blue, new CornerRadii(0), Insets(0)))))
            .otherwise(Background.Empty)
        }
      }
    }

    object Recurse {
      val stage: Stage = new Stage {
        scene = new Scene(600, 450) {
          stylesheets = List(getClass.getResource("styles.css").toExternalForm)
          root = new BorderPane {
            top = new ToolBar {
              items = Seq(
                new Label {
                  text <== Bindings.createStringBinding(() => currentInit.value.toString, currentInit)
                },
                new Button(">>") {
                  onAction = {
                    _ =>
                      val idx = (initValues.indexOf(currentInit.value) + 1) % initValues.length
                      currentInit.value = initValues(idx)
                  }
                },
                new Button("End") {
                  onAction = {
                    _ =>
                      stage.close()
                  }
                },
                new CheckBox("Give XP") {
                  selected <==> addXp
                }
              )
            }
            center = new VBox {
              margin = Insets(10)
              children = initValues.flatMap(makeInitiativeEntries)
            }
          }
        }
      }

      def run(): Unit = {
        stage.initModality(Modality.WindowModal)
        stage.showAndWait()
      }
    }
    Recurse.run()
    if (addXp.value) p.distributeXP(rating.xp / 2f) else Future.successful(p)
  }

  def getRunnableEncounter(p: CachedParty, enc: Encounter): RunnableEncounter = enc.start(initiativeChooserDialog(p))

  def initiativeChooserDialog(p: CachedParty): Seq[EncounterPartyMember] = {
    p.chars.map { c =>
      new ChoiceDialog[String]("Manual Initiative", Seq("Manual Initiaitve", "Automatic Initiative")) {
        headerText = s"Should ${c.name} roll their own initiative or should their initiative be automatic?"
        title = s"${c.name} Initiative"
      }.showAndWait() match {
        case Some("Manual Initiative") =>
          val init = new ChoiceDialog[Int](1 + c.init, 1 + c.init to 20 + c.init) {
            headerText = s"Enter Initiative Value for ${c.name}"
            title = s"${c.name} Initiative"
          }.showAndWait()
          ManualInit(init.getOrElse(1 + c.init), c)
        case _ =>
          AutoInit(c)
      }
    }
  }
}
