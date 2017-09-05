package charforgemgr

import java.io.{File, InputStreamReader}

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.drive.Drive
import com.google.api.services.sheets.v4.Sheets

import scala.collection.JavaConverters._

object Authenticator {
  lazy val cred: Credential = authenticate()
  private val dataStoreFactory: FileDataStoreFactory = new FileDataStoreFactory(new File(".charforgemgr"))
  private val transport: HttpTransport = GoogleNetHttpTransport.newTrustedTransport()
  private val scopes = Array("https://www.googleapis.com/auth/spreadsheets", "https://www.googleapis.com/auth/drive.readonly").toList.asJava
  private val factory = JacksonFactory.getDefaultInstance

  def authenticate(): Credential = {
    val secrets = GoogleClientSecrets.load(factory, new InputStreamReader(this.getClass.getResourceAsStream("client_secret.json")))
    val flow = new GoogleAuthorizationCodeFlow.Builder(transport, factory, secrets, scopes)
      .setDataStoreFactory(dataStoreFactory)
      .setAccessType("offline")
      .build()
    new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user")
  }

  def services(): (Sheets, Drive) = (
    new Sheets.Builder(transport, factory, cred)
      .setApplicationName("Charforge Manager")
      .build(),
    new Drive.Builder(transport, factory, cred)
      .setApplicationName("Charforge Manager")
      .build()
  )
}
