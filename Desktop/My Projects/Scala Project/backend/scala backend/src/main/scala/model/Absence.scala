package model

import traits.Validable
import scala.util.Try
import java.time.LocalDate

case class Absence(
                    idAbsence:    String,
                    matricule:    String,
                    matiere:      String,
                    dateAbsence:  LocalDate,
                    heures:       Int,
                    justifiee:    Boolean
                  ) extends Validable {

  override def isValid: Boolean =
    idAbsence.nonEmpty && matricule.nonEmpty &&
      matiere.nonEmpty   && heures > 0

  override def validate(): List[String] = List(
    Option.when(idAbsence.isEmpty)("L'identifiant de l'absence est vide"),
    Option.when(matricule.isEmpty)("Le matricule est vide"),
    Option.when(matiere.isEmpty)("La matière est vide"),
    Option.when(heures <= 0)(s"Les heures doivent être > 0, reçu: $heures")
  ).flatten

  def estJustifiee: Boolean = justifiee
  def heuresNonJustifiees: Int = if (!justifiee) heures else 0
}

object Absence {
  def fromCsvLine(line: String): Option[Absence] =
    Try {
      val parts = line.split(",")
      Option.when(parts.length >= 6)(
        Absence(
          idAbsence   = parts(0).trim,
          matricule   = parts(1).trim,
          matiere     = parts(2).trim,
          dateAbsence = LocalDate.parse(parts(3).trim),
          heures      = parts(4).trim.toInt,
          justifiee   = parts(5).trim.toLowerCase == "oui"
        )
      )
    }.getOrElse(None)
}

// DTOs résultats
case class AbsenceEtudiant(
                            matricule      : String,
                            nomComplet     : String,
                            totalHeures    : Int,
                            heuresJustifiees  : Int,
                            heuresNonJustifiees: Int,
                            enAlerte       : Boolean   // true si totalHeures > 10
                          )

case class TauxAbsenteisme(
                            filiere        : String,
                            totalEtudiants : Int,
                            totalHeures    : Int,
                            moyenneHeures  : Double,
                            taux           : Double    // % etudiants > 10h absence
                          )

