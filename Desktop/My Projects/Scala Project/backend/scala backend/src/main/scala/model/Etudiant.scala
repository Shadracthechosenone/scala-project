package universite.model

import java.time.LocalDate

// ─────────────────────────────────────────────
// TRAITS COMMUNS (utilises par plusieurs classes)
// ─────────────────────────────────────────────

/** Tout objet metier possede un identifiant unique */
trait Identifiable {
  def id: String
}

/** Tout objet peut s'afficher sous forme lisible */
trait Affichable {
  def afficher(): String
}

/** Tout objet peut valider ses propres donnees */
trait Validable {
  def estValide(): Boolean
}

// ─────────────────────────────────────────────
// ENUMERATION DU STATUT ETUDIANT
// ─────────────────────────────────────────────

sealed trait StatutEtudiant
object StatutEtudiant {
  case object Actif    extends StatutEtudiant
  case object Suspendu extends StatutEtudiant
  case object Diplome  extends StatutEtudiant
  case object Inactif extends StatutEtudiant

  /** Convertit une String (venant de la BDD) en StatutEtudiant */
  def fromString(s: String): StatutEtudiant = s.trim.toLowerCase match {
    case "actif"    => Actif
    case "suspendu" => Suspendu
    case "diplome"  => Diplome
    case autre      => throw new IllegalArgumentException(s"Statut inconnu : $autre")
  }

  /** Convertit un StatutEtudiant en String pour la BDD */
  def toString(statut: StatutEtudiant): String = statut match {
    case Actif    => "Actif"
    case Suspendu => "Suspendu"
    case Diplome  => "Diplome"
  }
}

// ─────────────────────────────────────────────
// MODELE ETUDIANT
// ─────────────────────────────────────────────

/**
 * Represente un etudiant dans le systeme universitaire.
 *
 * @param matricule      Identifiant unique (ex: ETU001)
 * @param nom            Nom de famille
 * @param prenom         Prenom
 * @param sexe           'M' ou 'F'
 * @param dateNaissance  Date de naissance
 * @param email          Adresse email (unique)
 * @param telephone      Numero de telephone (optionnel)
 * @param filiere        Nom de la filiere (ex: Informatique)
 * @param niveau         Niveau academique (ex: M1, M2)
 * @param anneeAcademique Annee en cours (ex: 2025-2026)
 * @param statut         Statut courant de l'etudiant
 */
case class Etudiant(
                     matricule:       String,
                     nom:             String,
                     prenom:          String,
                     sexe:            Char,
                     dateNaissance:   LocalDate,
                     email:           String,
                     telephone:       Option[String],      // Option : peut etre absent
                     filiere:         String,
                     niveau:          String,
                     anneeAcademique: String,
                     statut:          StatutEtudiant
                   ) extends Identifiable with Affichable with Validable {

  // ── Identifiable ──────────────────────────
  override def id: String = matricule

  // ── Affichable ────────────────────────────
  override def afficher(): String = {
    val tel = telephone.getOrElse("N/A")
    s"""
       |┌─────────────────────────────────────┐
       |  Etudiant : $prenom $nom
       |  Matricule : $matricule
       |  Sexe      : $sexe
       |  Naissance : $dateNaissance
       |  Email     : $email
       |  Telephone : $tel
       |  Filiere   : $filiere  |  Niveau : $niveau
       |  Annee     : $anneeAcademique
       |  Statut    : ${StatutEtudiant.toString(statut)}
       |└─────────────────────────────────────┘
    """.stripMargin
  }

  // ── Validable ─────────────────────────────
  override def estValide(): Boolean = {
    val emailRegex = """^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$""".r
    matricule.nonEmpty &&
      nom.nonEmpty        &&
      prenom.nonEmpty     &&
      (sexe == 'M' || sexe == 'F') &&
      emailRegex.matches(email) &&
      filiere.nonEmpty    &&
      niveau.nonEmpty     &&
      anneeAcademique.nonEmpty
  }

  // ── Methodes metier utiles ─────────────────

  /** Retourne le nom complet */
  def nomComplet: String = s"$prenom $nom"

  /** Verifie si l'etudiant est actif */
  def estActif: Boolean = statut == StatutEtudiant.Actif

  /** Verifie si l'etudiant est suspendu */
  def estSuspendu: Boolean = statut == StatutEtudiant.Suspendu
}