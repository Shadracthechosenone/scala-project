package universite.service

import model.Enseignant
import repository.EnseignantRepository
import scala.util.{Try, Failure, Success}

class EnseignantService(repo: EnseignantRepository) {

  def creer(e: Enseignant): Either[String, Enseignant] =
    if (repo.existe(e.idEnseignant)) Left(s"Enseignant ${e.idEnseignant} existe déjà")
    else repo.creer(e).toEither.left.map(_.getMessage)

  def trouverParId(id: String): Either[String, Enseignant] =
    repo.trouverParId(id).toRight(s"Enseignant $id introuvable")

  def tous(): List[Enseignant] = repo.tous()

  def modifier(e: Enseignant): Either[String, Enseignant] =
    if (!repo.existe(e.idEnseignant)) Left(s"Enseignant ${e.idEnseignant} introuvable")
    else repo.modifier(e).toEither.left.map(_.getMessage)

  def supprimer(id: String): Either[String, Boolean] =
    if (!repo.existe(id)) Left(s"Enseignant $id introuvable")
    else repo.supprimer(id).toEither.left.map(_.getMessage)
}