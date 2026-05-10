package repository

import universite.model._

class TableauDeBordRepository {

  // — nombre total d'étudiants
  def countEtudiants(): Int = 0

  // — nombre d'étudiants par filière
  def countEtudiantsParFiliere(): Map[String, Int] = Map.empty

  // — taux de réussite par filière (moyenne >= 10)
  def tauxReussiteParFiliere(): List[StatReussite] = List.empty

  // — moyenne générale par niveau
  def moyenneParNiveau(): List[StatMoyenneNiveau] = List.empty

  // — taux d'absentéisme global et par filière
  def tauxAbsenteisme(): StatAbsenteisme =
    StatAbsenteisme(0.0, Map.empty)

  // — classement général des étudiants par moyenne décroissante
  def classementEtudiants(): List[ClassementEtudiant] = List.empty

  // — matières ayant les plus faibles moyennes
  def matieresFaiblesMoyennes(limit: Int = 5): List[StatMatiereFaible] = List.empty

  // — enseignants ayant le plus grand volume horaire
  def enseignantsVolHoraire(limit: Int = 5): List[StatVolHoraireEnseignant] = List.empty

  // — étudiants à risque : moyenne < 10 OU absences > 10h
  def etudiantsARisque(): List[EtudiantRisque] = List.empty
}