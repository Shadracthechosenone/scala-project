package universite.service

import universite.model.Seance
import repository.EmploiDuTempsRepository

class EmploiDuTempsService(repo: EmploiDuTempsRepository) {

  def getAll(): List[Seance]                                   = repo.findAll()
  def getById(id: String): Either[String, Seance]             = repo.findById(id).toRight("Séance introuvable")
  def getByFiliere(f: String): List[Seance]                   = repo.findByFiliere(f)
  def getByNiveau(n: String): List[Seance]                    = repo.findByNiveau(n)
  def getByEnseignant(e: String): List[Seance]                = repo.findByEnseignant(e)
  def getBySalle(s: String): List[Seance]                     = repo.findBySalle(s)
  def getByFiliereAndNiveau(f: String, n: String): List[Seance] = repo.findByFiliereAndNiveau(f, n)
  def create(s: Seance): Either[String, Seance]               = repo.insert(s)
  def update(s: Seance): Either[String, Seance]               = repo.update(s)
  def delete(id: String): Either[String, Unit] =
    if (repo.delete(id)) Right(()) else Left("Séance introuvable")
}