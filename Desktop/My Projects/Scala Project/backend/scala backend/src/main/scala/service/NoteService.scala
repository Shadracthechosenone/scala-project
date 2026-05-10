package universite.service

import universite.model._
import repository.{NoteRepository, EtudiantRepository}
import com.typesafe.scalalogging.LazyLogging
import scala.util.{Failure, Success, Try}

class NoteService(
                   noteRepo:     NoteRepository,
                   etudiantRepo: EtudiantRepository
                 ) extends LazyLogging {

  // ── CRUD notes ────────────────────────────────────────────────────────

  def creerNote(note: Note): Try[Note] = {
    if (!note.isValid)
      return Failure(new IllegalArgumentException("Note invalide (valeurs hors 0-20 ou champs vides)."))
    if (noteRepo.existe(note.idNote))
      return Failure(new IllegalArgumentException(s"Note ${note.idNote} existe déjà."))
    if (!etudiantRepo.existe(note.matricule))
      return Failure(new NoSuchElementException(s"Etudiant ${note.matricule} introuvable."))
    noteRepo.creer(note)
  }

  // creer plusieurs notes

  def creerNotes(notes: List[Note]): List[Either[String, Note]] =
    notes.map { note =>
      creerNote(note) match {
        case Success(n)  => Right(n)
        case Failure(e)  => Left(s"${note.idNote} : ${e.getMessage}")
      }
    }


  def modifierNote(note: Note): Try[Note] = {
    if (!note.isValid)
      return Failure(new IllegalArgumentException("Note invalide."))
    if (!noteRepo.existe(note.idNote))
      return Failure(new NoSuchElementException(s"Note ${note.idNote} introuvable."))
    noteRepo.modifier(note)
  }

  def supprimerNote(idNote: String): Try[Boolean] = {
    if (!noteRepo.existe(idNote))
      return Failure(new NoSuchElementException(s"Note $idNote introuvable."))
    noteRepo.supprimer(idNote)
  }

  def chercherNote(idNote: String): Option[Note] =
    noteRepo.trouverParId(idNote)

  def notesParEtudiant(matricule: String): List[Note] =
    noteRepo.trouverParMatricule(matricule)

  def notesParMatiere(idMatiere: String): List[Note] =
    noteRepo.trouverParMatiere(idMatiere)

  // ── CALCULS ───────────────────────────────────────────────────────────

  /**
   * Calcule la moyenne d'une matière pour un étudiant.
   * Formule : 40% CC + 60% Examen (via Note.moyenneNote)
   */
  def moyenneMatiere(matricule: String, idMatiere: String): Option[Double] =
    noteRepo.trouverParMatricule(matricule)
      .find(_.matiere == idMatiere)
      .flatMap(_.moyenneNote)

  /**
   * Calcule la moyenne générale pondérée d'un étudiant.
   * Formule : Σ(noteMatiere * coefficient) / Σ(coefficients)
   */
  def moyenneGenerale(matricule: String): Option[Double] = {
    val notes    = noteRepo.trouverParMatricule(matricule)
    val matieres = noteRepo.toutesLesMatieres()

    val resultats = for {
      note    <- notes
      matiere <- matieres.find(_.idMatiere == note.matiere)
      moyenne <- note.moyenneNote
    } yield (moyenne * matiere.coefficient, matiere.coefficient)

    if (resultats.isEmpty) None
    else {
      val totalPondere     = resultats.map(_._1).sum
      val totalCoefficient = resultats.map(_._2).sum
      if (totalCoefficient == 0) None
      else Some(
        BigDecimal(totalPondere / totalCoefficient)
          .setScale(2, BigDecimal.RoundingMode.HALF_UP)
          .toDouble
      )
    }
  }

  /**
   * Détecte les notes invalides (hors 0-20).
   * Utile pour audit / correction.
   */
  def notesInvalides(): List[Note] =
    noteRepo.toutesLesNotes().filterNot(_.estValide)

  /**
   * Détecte les étudiants ajournés (moyenne générale < 10).
   * Retourne List[(matricule, moyenne)]
   */
  def etudiantsAjournes(): List[(String, Double)] =
    etudiantRepo.trouverTous().flatMap { etudiant =>
      moyenneGenerale(etudiant.matricule).collect {
        case moy if moy < 10.0 => (etudiant.matricule, moy)
      }
    }

  /**
   * Classe les étudiants par moyenne décroissante.
   * Retourne List[(rang, matricule, nomComplet, moyenne)]
   */
  def classerEtudiants(): List[(Int, String, String, Double)] =
    etudiantRepo.trouverTous()
      .flatMap { e =>
        moyenneGenerale(e.matricule).map(moy => (e.matricule, e.nomComplet, moy))
      }
      .sortBy(-_._3)
      .zipWithIndex
      .map { case ((mat, nom, moy), idx) => (idx + 1, mat, nom, moy) }

  /**
   * Produit le relevé de notes complet d'un étudiant.
   */
  def releveNotes(matricule: String): Option[ReleveNotes] =
    etudiantRepo.trouverParMatricule(matricule).map { etudiant =>
      val notes    = noteRepo.trouverParMatricule(matricule)
      val matieres = noteRepo.toutesLesMatieres()

      val resultats: List[ResultatMatiere] = matieres.map { mat =>
        val noteObtenue = notes.find(_.matiere == mat.idMatiere).flatMap(_.moyenneNote)
        ResultatMatiere(
          idMatiere     = mat.idMatiere,
          nomMatiere    = mat.nomMatiere,
          ue            = mat.ue,
          coefficient   = mat.coefficient,
          noteObtenue   = noteObtenue,
          notesPonderee = noteObtenue.map(_ * mat.coefficient)
        )
      }

      val moy = moyenneGenerale(matricule).getOrElse(0.0)

      val mention = moy match {
        case m if m >= 16 => "Très Bien"
        case m if m >= 14 => "Bien"
        case m if m >= 12 => "Assez Bien"
        case m if m >= 10 => "Passable"
        case _            => "Insuffisant"
      }

      ReleveNotes(
        matricule       = matricule,
        nomComplet      = etudiant.nomComplet,
        anneeAcademique = etudiant.anneeAcademique,
        resultats       = resultats,
        moyenneGenerale = moy,
        mention         = mention,
        decision        = if (moy >= 10) "Admis" else "Ajourné"
      )
    }

  /**
   * Bilan complet d'un étudiant avec rang optionnel.
   * Le rang est rempli séparément via classerEtudiants().
   */
  def bilanEtudiant(matricule: String): Option[BilanEtudiant] =
    etudiantRepo.trouverParMatricule(matricule).map { etudiant =>
      val notes    = noteRepo.trouverParMatricule(matricule)
      val matieres = noteRepo.toutesLesMatieres()

      val resultats: List[ResultatMatiere] = matieres.map { mat =>
        val noteObtenue = notes.find(_.matiere == mat.idMatiere).flatMap(_.moyenneNote)
        ResultatMatiere(
          idMatiere     = mat.idMatiere,
          nomMatiere    = mat.nomMatiere,
          ue            = mat.ue,
          coefficient   = mat.coefficient,
          noteObtenue   = noteObtenue,
          notesPonderee = noteObtenue.map(_ * mat.coefficient)
        )
      }

      val moy               = moyenneGenerale(matricule).getOrElse(0.0)
      val totalCoefficients = matieres.map(_.coefficient).sum

      BilanEtudiant(
        matricule         = matricule,
        nomComplet        = etudiant.nomComplet,
        moyenneGenerale   = moy,
        totalCoefficients = totalCoefficients,
        resultats         = resultats,
        estAjourne        = moy < 10.0,
        rang              = None
      )
    }
}