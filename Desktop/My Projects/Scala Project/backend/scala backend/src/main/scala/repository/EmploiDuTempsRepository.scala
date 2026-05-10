package repository

import universite.model.Seance

class EmploiDuTempsRepository {

  private val joursValides = Set("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi")

  private def parseMinutes(h: String): Int = {
    val parts = h.split(":")
    parts(0).toInt * 60 + parts(1).toInt
  }

  private def conflitHoraire(a: Seance, b: Seance): Boolean =
    a.jour == b.jour &&
      parseMinutes(a.heureDebut) < parseMinutes(b.heureFin) &&
      parseMinutes(b.heureDebut) < parseMinutes(a.heureFin)

  def findAll(): List[Seance]                        = List.empty
  def findById(id: String): Option[Seance]           = None
  def findByFiliere(f: String): List[Seance]         = List.empty
  def findByNiveau(n: String): List[Seance]          = List.empty
  def findByEnseignant(e: String): List[Seance]      = List.empty
  def findBySalle(s: String): List[Seance]           = List.empty
  def findByFiliereAndNiveau(f: String, n: String): List[Seance] = List.empty

  def checkConflits(s: Seance): List[String] = {
    val existantes = findAll()
    val conflitEnseignant = existantes
      .filter(e => e.enseignant == s.enseignant && e.idSeance != s.idSeance && conflitHoraire(e, s))
      .map(e => s"Conflit enseignant '${s.enseignant}' avec séance ${e.idSeance} (${e.jour} ${e.heureDebut}-${e.heureFin})")

    val conflitSalle = existantes
      .filter(e => e.salle == s.salle && e.idSeance != s.idSeance && conflitHoraire(e, s))
      .map(e => s"Conflit salle '${s.salle}' avec séance ${e.idSeance} (${e.jour} ${e.heureDebut}-${e.heureFin})")

    val conflitClasse = existantes
      .filter(e => e.filiere == s.filiere && e.niveau == s.niveau && e.idSeance != s.idSeance && conflitHoraire(e, s))
      .map(e => s"Conflit classe '${s.filiere} ${s.niveau}' avec séance ${e.idSeance} (${e.jour} ${e.heureDebut}-${e.heureFin})")

    conflitEnseignant ++ conflitSalle ++ conflitClasse
  }

  def insert(s: Seance): Either[String, Seance] = {
    if (!joursValides.contains(s.jour))
      return Left(s"Jour invalide : '${s.jour}'. Valeurs acceptées : ${joursValides.mkString(", ")}")
    if (parseMinutes(s.heureDebut) >= parseMinutes(s.heureFin))
      return Left("heure_debut doit être strictement inférieure à heure_fin")
    val conflits = checkConflits(s)
    if (conflits.nonEmpty) Left(conflits.mkString(" | "))
    else Right(s)
  }

  def update(s: Seance): Either[String, Seance] = {
    if (!joursValides.contains(s.jour))
      return Left(s"Jour invalide : '${s.jour}'")
    if (parseMinutes(s.heureDebut) >= parseMinutes(s.heureFin))
      return Left("heure_debut doit être strictement inférieure à heure_fin")
    val conflits = checkConflits(s)
    if (conflits.nonEmpty) Left(conflits.mkString(" | "))
    else Right(s)
  }

  def delete(id: String): Boolean = true
}