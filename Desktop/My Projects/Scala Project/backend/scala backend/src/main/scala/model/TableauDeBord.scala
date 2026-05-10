package universite.model

case class StatEtudiants(
                          total: Int,
                          parFiliere: Map[String, Int]
                        )

case class StatReussite(
                         filiere: String,
                         tauxReussite: Double
                       )

case class StatMoyenneNiveau(
                              niveau: String,
                              moyenne: Double
                            )

case class StatAbsenteisme(
                            tauxGlobal: Double,
                            parFiliere: Map[String, Double]
                          )

case class ClassementEtudiant(
                               rang: Int,
                               matricule: String,
                               nom: String,
                               moyenne: Double,
                               filiere: String,
                               niveau: String
                             )

case class StatMatiereFaible(
                              idMatiere: String,
                              nomMatiere: String,
                              moyenne: Double
                            )

case class StatVolHoraireEnseignant(
                                     idEnseignant: String,
                                     nomEnseignant: String,
                                     totalHeures: Double
                                   )

case class EtudiantRisque(
                           matricule: String,
                           nom: String,
                           filiere: String,
                           niveau: String,
                           moyenne: Double,
                           totalAbsences: Double,
                           raisonRisque: String
                         )