package universite.model

import traits.Validable
import traits.Calculable
import scala.util.Try

// ── Note ──────────────────────────────────────────────────────────────────────

case class Note(
                 idNote:          String,
                 matricule:       String,
                 matiere:         String,
                 controleContinu: Double,
                 examen:          Double
               ) extends Validable with Calculable {

  override def isValid: Boolean =
    idNote.nonEmpty      &&
      matricule.nonEmpty   &&
      matiere.nonEmpty     &&
      controleContinu >= 0 && controleContinu <= 20 &&
      examen          >= 0 && examen          <= 20

  override def validate(): List[String] = List(
    Option.when(idNote.isEmpty)("L'identifiant de la note est vide"),
    Option.when(matricule.isEmpty)("Le matricule est vide"),
    Option.when(matiere.isEmpty)("La matière est vide"),
    Option.when(controleContinu < 0 || controleContinu > 20)(
      s"Contrôle continu invalide: $controleContinu (attendu: 0–20)"
    ),
    Option.when(examen < 0 || examen > 20)(
      s"Note d'examen invalide: $examen (attendu: 0–20)"
    )
  ).flatten

  // 40% CC + 60% Examen, arrondi à 2 décimales
  override def calculer(): Double =
    BigDecimal(controleContinu * 0.4 + examen * 0.6)
      .setScale(2, BigDecimal.RoundingMode.HALF_UP)
      .toDouble

  // Retourne la moyenne uniquement si la note est valide
  def moyenneNote: Option[Double] =
    if (isValid) Some(calculer()) else None

  def estValide:    Boolean = isValid
  def estInvalide:  Boolean = !isValid
  def estManquante: Boolean = controleContinu < 0 || examen < 0
}

object Note {
  def fromCsvLine(line: String): Option[Note] =
    Try {
      val parts = line.split(",").map(_.trim)
      Option.when(parts.length >= 5)(
        Note(
          idNote          = parts(0),
          matricule       = parts(1),
          matiere         = parts(2),
          controleContinu = parts(3).toDouble,
          examen          = parts(4).toDouble
        )
      )
    }.getOrElse(None)
}


// ── ResultatMatiere ───────────────────────────────────────────────────────────

case class ResultatMatiere(
                            idMatiere:    String,
                            nomMatiere:   String,
                            ue:           String,
                            coefficient:  Int,
                            noteObtenue:  Option[Double],
                            notesPonderee: Option[Double]
                          )

// ── ReleveNotes ───────────────────────────────────────────────────────────────

case class ReleveNotes(
                        matricule:       String,
                        nomComplet:      String,
                        anneeAcademique: String,
                        resultats:       List[ResultatMatiere],
                        moyenneGenerale: Double,
                        mention:         String,
                        decision:        String
                      )

// ── BilanEtudiant ─────────────────────────────────────────────────────────────

case class BilanEtudiant(
                          matricule:         String,
                          nomComplet:        String,
                          moyenneGenerale:   Double,
                          totalCoefficients: Int,
                          resultats:         List[ResultatMatiere],
                          estAjourne:        Boolean,
                          rang:              Option[Int]
                        )