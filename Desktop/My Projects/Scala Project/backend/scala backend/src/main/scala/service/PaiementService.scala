// service/PaiementService.scala
package universite.service

import model.Paiement
import repository.{EtudiantRepository, PaiementRepository}

import scala.util.{Failure, Success, Try}

// Réponses métier
case class ResumePaiement(
                           matricule     : String,
                           montantTotal  : BigDecimal,
                           montantPaye   : BigDecimal,
                           resteAPayer   : BigDecimal,
                           estSolde      : Boolean,
                           paiements     : List[Paiement]
                         )

case class EtudiantEnDette(
                            matricule   : String,
                            nom         : String,
                            prenom      : String,
                            resteAPayer : BigDecimal
                          )

case class StatsPaiements(
                           totalAttendu      : BigDecimal,
                           totalEncaisse     : BigDecimal,
                           tauxRecouvrement  : Double,        // en %
                           nbEtudiantsEnDette: Int
                         )

class PaiementService(
                       paiementRepo : PaiementRepository,
                       etudiantRepo : EtudiantRepository
                     ) {

  // ── CRUD ────────────────────────────────────────────────────────────────

  def enregistrerPaiement(p: Paiement): Try[Paiement] = {
    if (p.montantTotal <= 0)
      return Failure(new IllegalArgumentException("Le montant total doit être positif."))
    if (p.montantPaye < 0)
      return Failure(new IllegalArgumentException("Le montant payé ne peut pas être négatif."))
    if (p.montantPaye > p.montantTotal)
      return Failure(new IllegalArgumentException("Le montant payé dépasse le montant total."))
    if (paiementRepo.existe(p.idPaiement))
      return Failure(new IllegalArgumentException(s"Paiement ${p.idPaiement} existe déjà."))
    paiementRepo.creer(p)
  }

  def chercherPaiement(idPaiement: String): Option[Paiement] =
    paiementRepo.trouverParId(idPaiement)

  def modifierPaiement(p: Paiement): Try[Paiement] = {
    if (!paiementRepo.existe(p.idPaiement))
      return Failure(new NoSuchElementException(s"Paiement ${p.idPaiement} introuvable."))
    if (p.montantPaye > p.montantTotal)
      return Failure(new IllegalArgumentException("Le montant payé dépasse le montant total."))
    paiementRepo.modifier(p)
  }

  def supprimerPaiement(idPaiement: String): Try[Boolean] = {
    if (!paiementRepo.existe(idPaiement))
      return Failure(new NoSuchElementException(s"Paiement $idPaiement introuvable."))
    paiementRepo.supprimer(idPaiement)
  }

  // ── MÉTIER ──────────────────────────────────────────────────────────────

  /** Résumé complet des paiements d'un étudiant */
  def resumePaiementsEtudiant(matricule: String): ResumePaiement = {
    val paiements    = paiementRepo.trouverParMatricule(matricule)
    val total        = paiements.map(_.montantTotal).sum
    val paye         = paiements.map(_.montantPaye).sum
    ResumePaiement(
      matricule    = matricule,
      montantTotal = total,
      montantPaye  = paye,
      resteAPayer  = total - paye,
      estSolde     = paye >= total && paiements.nonEmpty,
      paiements    = paiements
    )
  }

  /** Liste des étudiants ayant une dette avec leur montant restant */
  def etudiantsEnDette(): List[EtudiantEnDette] =
    paiementRepo.matriculesEnDette().flatMap { matricule =>
      etudiantRepo.trouverParMatricule(matricule).map { etudiant =>
        val paiements   = paiementRepo.trouverParMatricule(matricule)
        val resteAPayer = paiements.map(p => p.montantTotal - p.montantPaye).sum
        EtudiantEnDette(
          matricule   = matricule,
          nom         = etudiant.nom,
          prenom      = etudiant.prenom,
          resteAPayer = resteAPayer
        )
      }
    }

  /** Montant total encaissé (tous étudiants) */
  def montantTotalEncaisse(): BigDecimal =
    paiementRepo.totalEncaisseGlobal()

  /** Taux de recouvrement global = (encaissé / attendu) × 100 */
  def tauxRecouvrement(): Double = {
    val attendu   = paiementRepo.totalAttenduGlobal()
    val encaisse  = paiementRepo.totalEncaisseGlobal()
    if (attendu == 0) 0.0
    else ((encaisse / attendu) * 100).toDouble
  }

  /** Statistiques globales consolidées */
  def statistiquesGlobales(): StatsPaiements = {
    val attendu  = paiementRepo.totalAttenduGlobal()
    val encaisse = paiementRepo.totalEncaisseGlobal()
    val taux     = if (attendu == 0) 0.0 else ((encaisse / attendu) * 100).toDouble
    StatsPaiements(
      totalAttendu       = attendu,
      totalEncaisse      = encaisse,
      tauxRecouvrement   = taux,
      nbEtudiantsEnDette = paiementRepo.matriculesEnDette().size
    )
  }
}