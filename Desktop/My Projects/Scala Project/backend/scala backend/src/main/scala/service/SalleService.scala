package universite.service

import universite.model.Salle
import repository.SalleRepository

class SalleService(repo: SalleRepository) {

  def getAll(): List[Salle]                          = repo.findAll()
  def getById(id: String): Either[String, Salle]    = repo.findById(id).toRight("Salle introuvable")
  def getByType(t: String): List[Salle]             = repo.findByType(t)
  def create(s: Salle): Either[String, Salle]       = repo.insert(s)
  def update(s: Salle): Either[String, Salle]       = repo.update(s)
  def delete(id: String): Either[String, Unit] =
    if (repo.delete(id)) Right(()) else Left("Salle introuvable")
}