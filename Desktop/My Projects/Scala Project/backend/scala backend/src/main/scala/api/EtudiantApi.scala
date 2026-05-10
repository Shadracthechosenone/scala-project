package universite.api

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import spray.json._
import universite.model.{Etudiant, StatutEtudiant}
import universite.model.StatutEtudiant._
import universite.service.EtudiantService

import java.time.LocalDate
import scala.concurrent.ExecutionContext

object EtudiantJsonProtocol extends DefaultJsonProtocol {

  // 1. LocalDate
  implicit val localDateFormat: JsonFormat[LocalDate] = new JsonFormat[LocalDate] {
    override def write(obj: LocalDate): JsValue = JsString(obj.toString)
    override def read(json: JsValue): LocalDate = json match {
      case JsString(s) => LocalDate.parse(s)
      case _           => throw new DeserializationException("Date LocalDate invalide")
    }
  }

  // 2. StatutEtudiant — doit être déclaré AVANT etudiantFormat
  implicit val statutEtudiantFormat: JsonFormat[StatutEtudiant] =
    new JsonFormat[StatutEtudiant] {
      override def write(obj: StatutEtudiant): JsValue = JsString(obj.toString)
      override def read(json: JsValue): StatutEtudiant = json match {
        case JsString("Actif")    => Actif
        case JsString("Inactif")  => Inactif
        case JsString("Suspendu") => Suspendu
        case JsString("Diplome")  => Diplome
        case other => throw new DeserializationException(s"StatutEtudiant inconnu: $other")
      }
    }

  // 3. Etudiant — en dernier, après tous ses champs
  implicit val etudiantFormat: RootJsonFormat[Etudiant] = jsonFormat11(Etudiant.apply)
}