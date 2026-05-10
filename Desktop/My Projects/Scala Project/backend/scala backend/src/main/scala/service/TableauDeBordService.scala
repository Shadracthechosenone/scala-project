package universite.service

import universite.model._
import repository.TableauDeBordRepository

class TableauDeBordService(repo: TableauDeBordRepository) {

  def getStatEtudiants(): StatEtudiants =
    StatEtudiants(
      total      = repo.countEtudiants(),
      parFiliere = repo.countEtudiantsParFiliere()
    )

  def getTauxReussiteParFiliere(): List[StatReussite]          = repo.tauxReussiteParFiliere()
  def getMoyenneParNiveau(): List[StatMoyenneNiveau]           = repo.moyenneParNiveau()
  def getTauxAbsenteisme(): StatAbsenteisme                    = repo.tauxAbsenteisme()
  def getClassement(): List[ClassementEtudiant]                = repo.classementEtudiants()
  def getMatieresFaibles(limit: Int): List[StatMatiereFaible]  = repo.matieresFaiblesMoyennes(limit)
  def getEnseignantsVolHoraire(limit: Int): List[StatVolHoraireEnseignant] = repo.enseignantsVolHoraire(limit)
  def getEtudiantsARisque(): List[EtudiantRisque]              = repo.etudiantsARisque()
}