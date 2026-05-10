package universite.service

import model.{Absence, AbsenceEtudiant, TauxAbsenteisme}
import repository.{AbsenceRepository, EtudiantRepository}

import scala.util.{Failure, Success, Try}

class AbsenceService(
                      absenceRepo : AbsenceRepository,
                      etudiantRepo: EtudiantRepository
                    ) {

  private val SEUIL_ALERTE = 10

  // ── CRUD ────────────────────────────────────────────────────────────────

  def enregistrerAbsence(a: Absence): Try[Absence] = {
    if (a.idAbsence.isEmpty)
      return Failure(new IllegalArgumentException("L'identifiant est vide."))
    if (a.heures <= 0)
      return Failure(new IllegalArgumentException(s"Heures invalides : ${a.heures}."))
    if (absenceRepo.existe(a.idAbsence))
      return Failure(new IllegalArgumentException(s"Absence ${a.idAbsence} existe déjà."))
    if (!etudiantRepo.existe(a.matricule))
      return Failure(new NoSuchElementException(s"Etudiant ${a.matricule} introuvable."))
    absenceRepo.creer(a)
  }

  def enregistrerAbsences(absences: List[Absence]): List[Either[String, Absence]] =
    absences.map { a =>
      enregistrerAbsence(a) match {
        case Success(abs) => Right(abs)
        case Failure(e)   => Left(s"${a.idAbsence} : ${e.getMessage}")
      }
    }

  def modifierAbsence(a: Absence): Try[Absence] = {
    if (!absenceRepo.existe(a.idAbsence))
      return Failure(new NoSuchElementException(s"Absence ${a.idAbsence} introuvable."))
    absenceRepo.modifier(a)
  }

  def supprimerAbsence(idAbsence: String): Try[Boolean] = {
    if (!absenceRepo.existe(idAbsence))
      return Failure(new NoSuchElementException(s"Absence $idAbsence introuvable."))
    absenceRepo.supprimer(idAbsence)
  }

  def chercherAbsence(idAbsence: String): Option[Absence] =
    absenceRepo.trouverParId(idAbsence)

  // ── CALCULS ─────────────────────────────────────────────────────────────

  // Total heures d'absence par étudiant
  def totalHeuresAbsence(matricule: String): Int =
    absenceRepo.totalHeuresParMatricule(matricule)

  // Absences non justifiées (tous étudiants)
  def absencesNonJustifiees(): List[Absence] =
    absenceRepo.trouverNonJustifiees()

  // Absences non justifiées d'un étudiant
  def absencesNonJustifieesParEtudiant(matricule: String): List[Absence] =
    absenceRepo.trouverParMatricule(matricule).filterNot(_.justifiee)

  // Étudiants ayant dépassé le seuil de 10h
  def etudiantsEnAlerte(): List[AbsenceEtudiant] =
    etudiantRepo.trouverTous().flatMap { e =>
      val absences = absenceRepo.trouverParMatricule(e.matricule)
      val total    = absences.map(_.heures).sum
      if (total > SEUIL_ALERTE)
        Some(AbsenceEtudiant(
          matricule           = e.matricule,
          nomComplet          = e.nomComplet,
          totalHeures         = total,
          heuresJustifiees    = absences.filter(_.justifiee).map(_.heures).sum,
          heuresNonJustifiees = absences.filterNot(_.justifiee).map(_.heures).sum,
          enAlerte            = true
        ))
      else None
    }

  // Résumé absences d'un étudiant
  def resumeAbsencesEtudiant(matricule: String): Option[AbsenceEtudiant] =
    etudiantRepo.trouverParMatricule(matricule).map { e =>
      val absences = absenceRepo.trouverParMatricule(matricule)
      val total    = absences.map(_.heures).sum
      AbsenceEtudiant(
        matricule           = matricule,
        nomComplet          = e.nomComplet,
        totalHeures         = total,
        heuresJustifiees    = absences.filter(_.justifiee).map(_.heures).sum,
        heuresNonJustifiees = absences.filterNot(_.justifiee).map(_.heures).sum,
        enAlerte            = total > SEUIL_ALERTE
      )
    }

  // Taux d'absentéisme par filière
  def tauxAbsenteismeParFiliere(): List[TauxAbsenteisme] = {
    val etudiants = etudiantRepo.trouverTous()
    etudiants.groupBy(_.filiere).map { case (filiere, etudsFiliere) =>
      val heuresParEtud = etudsFiliere.map { e =>
        absenceRepo.totalHeuresParMatricule(e.matricule)
      }
      val totalHeures    = heuresParEtud.sum
      val nbEtudiants    = etudsFiliere.size
      val moyenneHeures  = if (nbEtudiants == 0) 0.0
      else BigDecimal(totalHeures.toDouble / nbEtudiants)
        .setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
      val nbEnAlerte     = heuresParEtud.count(_ > SEUIL_ALERTE)
      val taux           = if (nbEtudiants == 0) 0.0
      else BigDecimal(nbEnAlerte.toDouble / nbEtudiants * 100)
        .setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble

      TauxAbsenteisme(
        filiere        = filiere,
        totalEtudiants = nbEtudiants,
        totalHeures    = totalHeures,
        moyenneHeures  = moyenneHeures,
        taux           = taux
      )
    }.toList.sortBy(-_.taux)
  }
}