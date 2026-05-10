package universite.routes

import model.Paiement
import spray.json._
import universite.service.{EtudiantEnDette, ResumePaiement, StatsPaiements}

import java.time.LocalDate

trait PaiementJsonProtocol extends DefaultJsonProtocol {

  implicit object LocalDateFormat extends JsonFormat[LocalDate] {
    def write(d: LocalDate): JsValue = JsString(d.toString)
    def read(json: JsValue): LocalDate = json match {
      case JsString(s) => LocalDate.parse(s)
      case _           => deserializationError("Date attendue au format YYYY-MM-DD")
    }
  }

  implicit object OptionLocalDateFormat extends JsonFormat[Option[LocalDate]] {
    def write(opt: Option[LocalDate]): JsValue = opt match {
      case Some(d) => JsString(d.toString)
      case None    => JsNull
    }
    def read(json: JsValue): Option[LocalDate] = json match {
      case JsString(s) => Some(LocalDate.parse(s))
      case JsNull      => None
      case _           => deserializationError("Date attendue au format YYYY-MM-DD ou null")
    }
  }

  implicit val paiementFormat      : RootJsonFormat[Paiement]       = jsonFormat6(Paiement.apply)
  implicit val resumeFormat        : RootJsonFormat[ResumePaiement]  = jsonFormat6(ResumePaiement.apply)
  implicit val detteFormat         : RootJsonFormat[EtudiantEnDette] = jsonFormat4(EtudiantEnDette.apply)
  implicit val statsFormat         : RootJsonFormat[StatsPaiements]  = jsonFormat4(StatsPaiements.apply)

  case class PaiementErrorResponse(success: Boolean, error: String)
  case class PaiementMessageResponse(message: String)
  case class DettesResponse(count: Int, etudiants: List[EtudiantEnDette])

  implicit val errorResponseFormat  : RootJsonFormat[PaiementErrorResponse]   = jsonFormat2(PaiementErrorResponse.apply)
  implicit val messageResponseFormat: RootJsonFormat[PaiementMessageResponse]  = jsonFormat1(PaiementMessageResponse.apply)
  implicit val dettesResponseFormat : RootJsonFormat[DettesResponse]           = jsonFormat2(DettesResponse.apply)
}