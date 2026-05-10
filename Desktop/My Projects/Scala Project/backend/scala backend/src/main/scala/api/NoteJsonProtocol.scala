package universite.routes

import spray.json._
import spray.json.DefaultJsonProtocol._
import universite.model._

// ── DTOs réponse ──────────────────────────────────────────────────────────────

case class ErrorResponse(success: Boolean, error: String)
case class MessageResponse(message: String)
case class MoyenneMatiereResponse(matricule: String, idMatiere: String, moyenne: Double)
case class MoyenneGeneraleResponse(matricule: String, moyenneGenerale: Double)
case class AjourneItem(matricule: String, moyenneGenerale: Double)
case class AjournesResponse(count: Int, etudiants: List[AjourneItem])
case class ClassementItem(rang: Int, matricule: String, nomComplet: String, moyenne: Double)
case class ClassementResponse(classement: List[ClassementItem])
case class NotesListResponse(count: Int, notes: List[Note])
case class BulkNoteResponse(
                             succes:  List[Note],
                             echecs:  List[String],
                             total:   Int,
                             reussis: Int,
                             echoues: Int
                           )
// ── Protocol ──────────────────────────────────────────────────────────────────

trait NoteJsonProtocol extends DefaultJsonProtocol {

  // ── Option[Int] / Option[Double] / Option[String] ────────────────────────
  // spray-json gère Option nativement via optionFormat — pas besoin de le déclarer

  // ── Note : 5 champs ──────────────────────────────────────────────────────
  // idNote, matricule, matiere, controleContinu, examen
  implicit val noteFormat: RootJsonFormat[Note] = jsonFormat5(Note.apply)

  // ── Matiere : 6 champs ───────────────────────────────────────────────────
  // idMatiere, nomMatiere, ue, coefficient, volumeHoraire, enseignant
  implicit val matiereFormat: RootJsonFormat[Matiere] = jsonFormat6(Matiere.apply)

  // ── ResultatMatiere : 6 champs ───────────────────────────────────────────
  // idMatiere, nomMatiere, ue, coefficient, noteObtenue, notesPonderee
  implicit val resultatMatiereFormat: RootJsonFormat[ResultatMatiere] = jsonFormat6(ResultatMatiere.apply)

  // ── ReleveNotes : 7 champs ───────────────────────────────────────────────
  // matricule, nomComplet, anneeAcademique, resultats, moyenneGenerale, mention, decision
  implicit val releveNotesFormat: RootJsonFormat[ReleveNotes] = jsonFormat7(ReleveNotes.apply)

  // ── BilanEtudiant : 7 champs ─────────────────────────────────────────────
  // matricule, nomComplet, moyenneGenerale, totalCoefficients, resultats, estAjourne, rang
  implicit val bilanEtudiantFormat: RootJsonFormat[BilanEtudiant] = jsonFormat7(BilanEtudiant.apply)

  // ── DTOs réponse ─────────────────────────────────────────────────────────
  implicit val errorFormat      : RootJsonFormat[ErrorResponse]           = jsonFormat2(ErrorResponse.apply)
  implicit val messageFormat    : RootJsonFormat[MessageResponse]         = jsonFormat1(MessageResponse.apply)
  implicit val moyMatFormat     : RootJsonFormat[MoyenneMatiereResponse]  = jsonFormat3(MoyenneMatiereResponse.apply)
  implicit val moyGenFormat     : RootJsonFormat[MoyenneGeneraleResponse] = jsonFormat2(MoyenneGeneraleResponse.apply)
  implicit val ajourneItemFormat: RootJsonFormat[AjourneItem]             = jsonFormat2(AjourneItem.apply)
  implicit val ajournesFormat   : RootJsonFormat[AjournesResponse]        = jsonFormat2(AjournesResponse.apply)
  implicit val classItemFormat  : RootJsonFormat[ClassementItem]          = jsonFormat4(ClassementItem.apply)
  implicit val classementFormat : RootJsonFormat[ClassementResponse]      = jsonFormat1(ClassementResponse.apply)
  implicit val notesListFormat  : RootJsonFormat[NotesListResponse]       = jsonFormat2(NotesListResponse.apply)

  implicit val bulkNoteResponseFormat: RootJsonFormat[BulkNoteResponse] = jsonFormat5(BulkNoteResponse.apply)
}