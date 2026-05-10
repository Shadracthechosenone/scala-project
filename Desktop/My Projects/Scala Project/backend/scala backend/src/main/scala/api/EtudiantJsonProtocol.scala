package api

import spray.json._
import universite.model.{Etudiant, StatutEtudiant}
import java.time.LocalDate

object EtudiantJsonProtocol extends DefaultJsonProtocol {

  // Sérialisation de LocalDate
  implicit object LocalDateFormat extends JsonFormat[LocalDate] {
    def write(d: LocalDate): JsValue = JsString(d.toString)
    def read(v: JsValue): LocalDate = v match {
      case JsString(s) => LocalDate.parse(s)
      case _           => deserializationError("Date invalide, format attendu: YYYY-MM-DD")
    }
  }

  // Sérialisation de Char (pour le sexe)
  implicit object CharFormat extends JsonFormat[Char] {
    def write(c: Char): JsValue = JsString(c.toString)
    def read(v: JsValue): Char = v match {
      case JsString(s) if s.length == 1 => s.head
      case _ => deserializationError("Caractère invalide, attendu: 'M' ou 'F'")
    }
  }

  // Sérialisation de StatutEtudiant (sealed trait)
  implicit object StatutFormat extends JsonFormat[StatutEtudiant] {
    def write(s: StatutEtudiant): JsValue = s match {
      case StatutEtudiant.Actif    => JsString("Actif")
      case StatutEtudiant.Suspendu => JsString("Suspendu")
      case StatutEtudiant.Diplome  => JsString("Diplome")
      case StatutEtudiant.Inactif  => JsString("Inactif")
    }
    def read(v: JsValue): StatutEtudiant = v match {
      case JsString(s) => StatutEtudiant.fromString(s)
      case _           => deserializationError("Statut invalide")
    }
  }

  // Sérialisation complète d'Etudiant
  implicit val etudiantFormat: RootJsonFormat[Etudiant] = jsonFormat11(Etudiant.apply)
}