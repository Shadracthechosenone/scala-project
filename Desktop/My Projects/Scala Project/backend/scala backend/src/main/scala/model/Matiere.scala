package universite.model

/**
 * Représente une matière dans le système universitaire.
 *
 * @param idMatiere     Identifiant unique (ex: MAT001)
 * @param nomMatiere    Nom complet de la matière (ex: Algorithmique)
 * @param ue            Unité d'enseignement (ex: UE Informatique)
 * @param coefficient   Coefficient de la matière pour le calcul de moyenne
 * @param volumeHoraire Volume horaire total en heures
 * @param enseignant    Identifiant de l'enseignant responsable (optionnel)
 */
case class Matiere(
                    idMatiere:     String,
                    nomMatiere:    String,
                    ue:            String,
                    coefficient:   Int,
                    volumeHoraire: Int,
                    enseignant:    Option[String]
                  ) extends Identifiable with Affichable with Validable {

  // ── Identifiable ──────────────────────────
  override def id: String = idMatiere

  // ── Affichable ────────────────────────────
  override def afficher(): String = {
    val ens = enseignant.getOrElse("Non assigné")
    s"""
       |┌─────────────────────────────────────┐
       |  Matière      : $nomMatiere
       |  ID           : $idMatiere
       |  UE           : $ue
       |  Coefficient  : $coefficient
       |  Vol. horaire : $volumeHoraire h
       |  Enseignant   : $ens
       |└─────────────────────────────────────┘
    """.stripMargin
  }

  // ── Validable ─────────────────────────────
  override def estValide(): Boolean =
    idMatiere.nonEmpty     &&
      nomMatiere.nonEmpty    &&
      ue.nonEmpty            &&
      coefficient > 0        &&
      volumeHoraire > 0
}