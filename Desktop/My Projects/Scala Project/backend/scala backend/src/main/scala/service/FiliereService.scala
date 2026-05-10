package universite.service

import universite.model.Filiere
import universite.repository.FiliereRepository

class FiliereService(repo: FiliereRepository) {

  def getAll(): List[Filiere] = repo.findAll()

  def getById(id: String): Either[String, Filiere] =
    repo.findById(id).toRight("Filière introuvable")

  def create(f: Filiere): Either[String, Filiere] = repo.insert(f)

  def update(f: Filiere): Either[String, Filiere] = repo.update(f)

  def delete(id: String): Either[String, Unit] =
    if (repo.delete(id)) Right(()) else Left("Filière introuvable")
}