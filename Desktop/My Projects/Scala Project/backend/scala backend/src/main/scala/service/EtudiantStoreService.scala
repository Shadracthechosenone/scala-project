package universite.service

import universite.model.{Etudiant, StatutEtudiant}
import repository.EtudiantRepository
import com.typesafe.scalalogging.LazyLogging
import scala.util.{Failure, Success, Try}

class EtudiantService(repository: EtudiantRepository) extends LazyLogging {

  // ─────────────────────────────────────────────────
  // CREATION
  // ─────────────────────────────────────────────────

  def creerEtudiant(etudiant: Etudiant): Try[Etudiant] = {
    if (!etudiant.estValide())
      return Failure(new IllegalArgumentException("Les donnees de l'etudiant sont invalides."))

    if (repository.existe(etudiant.matricule))
      return Failure(new IllegalArgumentException(s"Le matricule ${etudiant.matricule} est deja utilise."))

    if (repository.emailExiste(etudiant.email))
      return Failure(new IllegalArgumentException(s"L'email ${etudiant.email} est deja utilise."))

    repository.creer(etudiant) match {
      case Success(e) =>
        logger.info(s"Etudiant cree : ${e.matricule}")
        Success(e)
      case Failure(ex) =>
        logger.error(s"Erreur creation etudiant : ${ex.getMessage}")
        Failure(ex)
    }
  }

  // ─────────────────────────────────────────────────
  // RECHERCHE
  // ─────────────────────────────────────────────────

  def rechercherParMatricule(matricule: String): Option[Etudiant] = {
    if (matricule.isBlank) {
      logger.warn("Matricule vide fourni a rechercherParMatricule")
      None
    } else {
      repository.trouverParMatricule(matricule.trim.toUpperCase)
    }
  }

  def listerTous(): List[Etudiant] = repository.trouverTous()

  def filtrerParFiliere(filiere: String): List[Etudiant] =
    repository.trouverParFiliere(filiere.trim)

  def filtrerParNiveau(niveau: String): List[Etudiant] =
    repository.trouverParNiveau(niveau.trim.toUpperCase)

  def listerActifs(): List[Etudiant] =
    repository.trouverParStatut("Actif")

  def listerSuspendus(): List[Etudiant] =
    repository.trouverParStatut("Suspendu")

  // ── NOUVEAU : filtrer par StatutEtudiant (sealed trait) ──────────────
  // Utilisé par EtudiantRoutes pour le paramètre ?statut=XXX
  def filtrerParStatut(statut: StatutEtudiant): List[Etudiant] =
    repository.trouverParStatut(StatutEtudiant.toString(statut))

  // ─────────────────────────────────────────────────
  // MODIFICATION
  // ─────────────────────────────────────────────────

  def modifierEtudiant(etudiant: Etudiant): Try[Etudiant] = {
    if (!etudiant.estValide())
      return Failure(new IllegalArgumentException("Donnees invalides."))

    if (!repository.existe(etudiant.matricule))
      return Failure(new NoSuchElementException(s"Etudiant ${etudiant.matricule} introuvable."))

    if (repository.emailExiste(etudiant.email, Some(etudiant.matricule)))
      return Failure(new IllegalArgumentException(s"L'email ${etudiant.email} est deja pris."))

    repository.modifier(etudiant)
  }

  def changerStatut(matricule: String, nouveauStatut: String): Try[Etudiant] = {
    Try(StatutEtudiant.fromString(nouveauStatut)) match {
      case Failure(_) =>
        Failure(new IllegalArgumentException(s"Statut invalide : $nouveauStatut"))
      case Success(statut) =>
        rechercherParMatricule(matricule) match {
          case None =>
            Failure(new NoSuchElementException(s"Etudiant $matricule introuvable."))
          case Some(etudiant) =>
            repository.modifier(etudiant.copy(statut = statut))
        }
    }
  }

  // ─────────────────────────────────────────────────
  // SUPPRESSION
  // ─────────────────────────────────────────────────

  def supprimerEtudiant(matricule: String): Try[Boolean] = {
    if (!repository.existe(matricule))
      return Failure(new NoSuchElementException(s"Etudiant $matricule introuvable."))
    repository.supprimer(matricule)
  }

  // ─────────────────────────────────────────────────
  // TRAITEMENTS FONCTIONNELS
  // ─────────────────────────────────────────────────

  def compterActifs(): Int =
    repository.compterParStatut("Actif")   // ← direct BDD, plus efficace que listerTous().filter

  def nomsComplets(): List[String] =
    listerTous().map(_.nomComplet)

  def grouperParFiliere(): Map[String, List[Etudiant]] =
    listerTous().groupBy(_.filiere)

  def grouperParNiveau(): Map[String, List[Etudiant]] =
    listerTous().groupBy(_.niveau)

  def compterParFiliere(): Map[String, Int] =
    grouperParFiliere().map { case (filiere, liste) => filiere -> liste.length }

  def rechercherRecursif(matricule: String, liste: List[Etudiant]): Option[Etudiant] =
    liste match {
      case Nil                                          => None
      case tete :: _ if tete.matricule == matricule     => Some(tete)
      case _ :: reste                                   => rechercherRecursif(matricule, reste)
    }

  def produireRapport(): String = {
    val tous      = listerTous()
    val actifs    = compterActifs()                          // ← utilise la BDD directement
    val suspendus = tous.filter(_.estSuspendu).length
    val parFil    = compterParFiliere()
      .map { case (f, n) => s"  - $f : $n etudiant(s)" }
      .mkString("\n")

    s"""
       |========================================
       |  RAPPORT MODULE ETUDIANTS
       |========================================
       |  Total etudiants    : ${tous.length}
       |  Actifs             : $actifs
       |  Suspendus          : $suspendus
       |
       |  Repartition par filiere :
       |$parFil
       |========================================
    """.stripMargin
  }
}