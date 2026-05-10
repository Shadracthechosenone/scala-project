package universite.routes

import model.{Absence, AbsenceEtudiant, TauxAbsenteisme}
import spray.json._
import spray.json.DefaultJsonProtocol._
import universite.model._

import java.time.LocalDate

case class AbsenceErrorResponse(success: Boolean, error: String)
case class AbsenceMessageResponse(message: String)
case class TotalHeuresResponse(matricule: String, totalHeures: Int)
case class AlertesResponse(count: Int, etudiants: List[AbsenceEtudiant])
case class BulkAbsenceResponse(succes: List[Absence], echecs: List[String], total: Int, reussis: Int, echoues: Int)

trait AbsenceJsonProtocol extends DefaultJsonProtocol {

  // LocalDate format
  implicit val localDateFormat: JsonFormat[LocalDate] = new JsonFormat[LocalDate] {
    def write(d: LocalDate): JsValue = JsString(d.toString)
    def read(v: JsValue): LocalDate  = v match {
      case JsString(s) => LocalDate.parse(s)
      case _           => deserializationError("Date attendue au format YYYY-MM-DD")
    }
  }

  implicit val absenceFormat        : RootJsonFormat[Absence]             = jsonFormat(Absence.apply, "idAbsence", "matricule", "matiere", "dateAbsence", "heures", "justifiee")
  implicit val absenceEtudiantFormat: RootJsonFormat[AbsenceEtudiant]     = jsonFormat(AbsenceEtudiant.apply, "matricule", "nomComplet", "totalHeures", "heuresJustifiees", "heuresNonJustifiees", "enAlerte")
  implicit val tauxFormat           : RootJsonFormat[TauxAbsenteisme]     = jsonFormat(TauxAbsenteisme.apply, "filiere", "totalEtudiants", "totalHeures", "moyenneHeures", "taux")
  implicit val errorFormat          : RootJsonFormat[AbsenceErrorResponse] = jsonFormat2(AbsenceErrorResponse.apply)
  implicit val messageFormat        : RootJsonFormat[AbsenceMessageResponse] = jsonFormat1(AbsenceMessageResponse.apply)
  implicit val totalFormat          : RootJsonFormat[TotalHeuresResponse]  = jsonFormat2(TotalHeuresResponse.apply)
  implicit val alertesFormat        : RootJsonFormat[AlertesResponse]      = jsonFormat2(AlertesResponse.apply)
  implicit val bulkFormat           : RootJsonFormat[BulkAbsenceResponse]  = jsonFormat5(BulkAbsenceResponse.apply)
}