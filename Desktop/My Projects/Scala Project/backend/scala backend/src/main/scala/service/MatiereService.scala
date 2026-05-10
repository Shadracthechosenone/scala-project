package universite.service

import universite.model.Matiere
import repository.MatiereRepository

class MatiereService(repo: MatiereRepository) {

  def creer(m: Matiere): Either[String, Matiere] =
    if (repo.existe(m.idMatiere)) Left(s"Matière ${m.idMatiere} existe déjà")
    else repo.creer(m).toEither.left.map(_.getMessage)

  def trouverParId(id: String): Either[String, Matiere] =
    repo.trouverParId(id).toRight(s"Matière $id introuvable")

  def toutes(): List[Matiere] = repo.toutes()

  def parEnseignant(id: String): List[Matiere] = repo.parEnseignant(id)

  def modifier(m: Matiere): Either[String, Matiere] =
    if (!repo.existe(m.idMatiere)) Left(s"Matière ${m.idMatiere} introuvable")
    else repo.modifier(m).toEither.left.map(_.getMessage)

  def supprimer(id: String): Either[String, Boolean] =
    if (!repo.existe(id)) Left(s"Matière $id introuvable")
    else repo.supprimer(id).toEither.left.map(_.getMessage)
}