package universite.service

import universite.model.Inscription
import repository.InscriptionRepository

class InscriptionService(repo: InscriptionRepository) {

  def getAll(): List[Inscription]                          = repo.findAll()
  def getById(id: String): Either[String, Inscription]    = repo.findById(id).toRight("Inscription introuvable")
  def getByMatricule(m: String): List[Inscription]        = repo.findByMatricule(m)
  def getByFiliere(f: String): List[Inscription]          = repo.findByFiliere(f)
  def create(i: Inscription): Either[String, Inscription] = repo.insert(i)
  def update(i: Inscription): Either[String, Inscription] = repo.update(i)
  def delete(id: String): Either[String, Unit] =
    if (repo.delete(id)) Right(()) else Left("Inscription introuvable")
}