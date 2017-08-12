package charforgemgr.ui

import java.io.FileInputStream

import charforgemgr._
import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets
import play.api.libs.json.{Format, JsError, JsSuccess, Json}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Success}
import scalafx.Includes._
import scalafx.application
import scalafx.application.{JFXApp, Platform}
import scalafx.beans.binding.Bindings
import scalafx.beans.property.ObjectProperty
import scalafx.collections.{ObservableBuffer, ObservableMap}
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control._
import scalafx.scene.layout.{BorderPane, Priority, VBox}
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

object UIApp extends JFXApp {
  implicit val (sheets: Sheets, drive: Drive) = Authenticator.services()
  implicit val executor: ExecutionContextExecutor = ExecutionContext.fromExecutor(Platform.runLater _)

  val party = new ObjectProperty[CachedParty] {
    value = CachedParty("No Party", Seq.empty)
  }
  val partyMembers: ObservableBuffer[CachedCharacter] = ObservableBuffer.empty[CachedCharacter]
  party.onChange { (_, _, v) =>
    partyMembers.clear()
    partyMembers ++= v.chars
  }

  val monsterLibrary: ObservableMap[String, Monster] = ObservableMap.empty[String, Monster]
  val mobs: ObservableBuffer[Monster] = ObservableBuffer.empty
  monsterLibrary.onChange { (_, c) =>
    c match {
      case ObservableMap.Add(_, v) => mobs += v
      case ObservableMap.Remove(_, v) => mobs -= v
      case ObservableMap.Replace(_, n, o) =>
        mobs.replaceAll(o, n)
    }
  }

  val encounters: ObservableBuffer[Encounter] = ObservableBuffer.empty[Encounter]

  stage = new application.JFXApp.PrimaryStage {
    title = "CharForge Manager"
    scene = new Scene(800, 650) {
      stylesheets = List(getClass.getResource("styles.css").toExternalForm)
      val menu = new MenuBar {
        menus = List(
          new Menu("File") {
            items = List(
              new MenuItem("Open Party...") {
                onAction = {
                  e: ActionEvent =>
                    val chooser = new FileChooser {
                      title = "Open Party File"
                      extensionFilters.add(new ExtensionFilter("Party Files", "*.party"))
                    }
                    chooser.showOpenDialog(stage) match {
                      case null =>
                      case f => Json.fromJson[Party](Json.parse(new FileInputStream(f))) match {
                        case JsSuccess(p, _) =>
                          p.preLoad.onComplete({
                            case Success(p1) =>
                              party.value = p1
                            case Failure(ex) =>
                              new Alert(AlertType.Error) {
                                initOwner(stage)
                                title = "Network Error"
                                headerText = "Could not load player data"
                                contentText = ex.getMessage
                              }.showAndWait()
                          })(executor)
                        case JsError(_) =>
                          new Alert(AlertType.Error) {
                            initOwner(stage)
                            title = "File Error"
                            contentText = "That does not appear to be a valid party file!"
                          }.showAndWait()
                      }
                    }
                }
              },
              new MenuItem("Open Monster(s)...") {
                onAction = {
                  e: ActionEvent =>
                    val chooser = new FileChooser {
                      title = "Open Monster File"
                      extensionFilters.add(new ExtensionFilter("Monster Files", "*.mobs"))
                    }
                    chooser.showOpenDialog(stage) match {
                      case null =>
                      case f => Json.fromJson[Map[String, Monster]](Json.parse(new FileInputStream(f))) match {
                        case JsSuccess(ml, _) =>
                          monsterLibrary ++= ml
                        case JsError(_) =>
                          new Alert(AlertType.Error) {
                            initOwner(stage)
                            title = "File Error"
                            contentText = "That does not appear to be a valid monster file!"
                          }.showAndWait()
                      }
                    }
                }
              },
              new MenuItem("Open Encounter...") {
                onAction = {
                  e: ActionEvent =>
                    val chooser = new FileChooser {
                      title = "Open Encounter File"
                      extensionFilters.add(new ExtensionFilter("Encounter Files", "*.encounter"))
                    }
                    implicit val encFmt: Format[Encounter] = Encounter.encFormat(monsterLibrary)
                    chooser.showOpenDialog(stage) match {
                      case null =>
                      case f => Json.fromJson[Encounter](Json.parse(new FileInputStream(f))) match {
                        case JsSuccess(enc, _) =>
                          encounters += enc
                        case JsError(_) =>
                          new Alert(AlertType.Error) {
                            initOwner(stage)
                            title = "File Error"
                            contentText = "That does not appear to be a valid encounter file!"
                          }.showAndWait()
                      }
                    }
                }
              }
            )
          }
        )
      }

      root = new BorderPane {
        hgrow = Priority.Always
        vgrow = Priority.Always

        top = menu

        left = new VBox {
          vgrow = Priority.Always
          children = List(new Label {
            padding = Insets(5)
            text <== Bindings.createStringBinding(() => party.value.name, party)
          }, new ScrollPane {
            fitToWidth = true
            fitToHeight = true
            vgrow = Priority.Always
            content = new ListView[CachedCharacter] {
              items = partyMembers
              vgrow = Priority.Always
              cellFactory = { _ =>
                new ListCell[CachedCharacter] {
                  item.onChange { (_, _, c) =>
                    if (c != null)
                      graphic = CharacterRender.makeCell(c)
                  }
                }
              }
            }
          })
        }

        center = new TabPane {
          val tabEncounter = new Tab {
            closable = false
            text = "Encounters"
            content = new ScrollPane {
              fitToHeight = true
              fitToWidth = true
              vgrow = Priority.Always
              hgrow = Priority.Always
              content = new ListView[Encounter] {
                items = encounters
                vgrow = Priority.Always
                hgrow = Priority.Always
                cellFactory = { _ =>
                  new ListCell[Encounter] {
                    hgrow = Priority.Always
                    item.onChange { (_, _, e) =>
                      if (e != null)
                        graphic = EncounterRender.makeCell(e, { () =>
                          EncounterDialog.runEncounter(party.value, e).onComplete {
                            case Success(p) => party.value = p
                            case Failure(ex) => println(ex)
                          }(executor)
                        })
                    }
                  }
                }
              }
            }
          }
          val tabMonsterLibrary = new Tab {
            closable = false
            text = "Monsters"
            content = new VBox {
              children = Seq(
                new ToolBar {
                  items = Seq(
                    new Button("+") {
                      onAction = { _ =>
                        MonsterBuilder.makeMonster() match {
                          case Some(m) =>
                            monsterLibrary.put(m.name, m)
                          case None =>
                        }
                      }
                    }
                  )
                },
                new ScrollPane {
                  fitToWidth = true
                  fitToHeight = true
                  vgrow = Priority.Always
                  hgrow = Priority.Always
                  content = new ListView[Monster] {
                    items = mobs
                    vgrow = Priority.Always
                    hgrow = Priority.Always
                    cellFactory = { _ =>
                      new ListCell[Monster] {
                        hgrow = Priority.Always
                        item.onChange { (_, _, m) =>
                          if (m != null)
                            graphic = MonsterRender.makeCell(m)
                        }
                      }
                    }
                  }
                })
            }
          }

          tabs = Seq(tabEncounter, tabMonsterLibrary)
        }
      }
    }
  }
}
