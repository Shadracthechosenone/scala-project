package api

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import repository.{AbsenceRepository, EnseignantRepository, EtudiantRepository, MatiereRepository, NoteRepository, PaiementRepository, InscriptionRepository, EmploiDuTempsRepository, SalleRepository, TableauDeBordRepository}
import universite.service.{AbsenceService, EnseignantService, EtudiantService, FiliereService, InscriptionService, MatiereService, NoteService, PaiementService, EmploiDuTempsService, SalleService, TableauDeBordService}
import universite.routes.{AbsenceRoutes, EnseignantRoutes, FiliereRoutes, InscriptionRoutes, MatiereRoutes, NoteRoutes, PaiementRoutes, EmploiDuTempsRoutes, SalleRoutes, TableauDeBordRoutes}
import api.EtudiantRoutes
import config.DatabaseConnection
import universite.repository.FiliereRepository

import scala.util.{Failure, Success}

object ApiServer {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem(Behaviors.empty, "universite-api")
    implicit val ec     = system.executionContext

    // Repositories
    val etudiantRepo        = new EtudiantRepository()
    val noteRepo            = new NoteRepository()
    val absenceRepo         = new AbsenceRepository()
    val paiementRepo        = new PaiementRepository()
    val enseignantRepo      = new EnseignantRepository()
    val matiereRepo         = new MatiereRepository()
    val filiereRepo         = new FiliereRepository()
    val inscriptionRepo     = new InscriptionRepository()
    val emploiDuTempsRepo   = new EmploiDuTempsRepository()
    val salleRepo           = new SalleRepository()
    val tableauDeBordRepo   = new TableauDeBordRepository()

    // Services
    val etudiantService        = new EtudiantService(etudiantRepo)
    val noteService            = new NoteService(noteRepo, etudiantRepo)
    val absenceService         = new AbsenceService(absenceRepo, etudiantRepo)
    val paiementService        = new PaiementService(paiementRepo, etudiantRepo)
    val enseignantService      = new EnseignantService(enseignantRepo)
    val matiereService         = new MatiereService(matiereRepo)
    val filiereService         = new FiliereService(filiereRepo)
    val inscriptionService     = new InscriptionService(inscriptionRepo)
    val emploiDuTempsService   = new EmploiDuTempsService(emploiDuTempsRepo)
    val salleService           = new SalleService(salleRepo)
    val tableauDeBordService   = new TableauDeBordService(tableauDeBordRepo)

    // Routes
    val etudiantRoutes        = new EtudiantRoutes(etudiantService)
    val noteRoutes            = new NoteRoutes(noteService)
    val absenceRoutes         = new AbsenceRoutes(absenceService)
    val paiementRoutes        = new PaiementRoutes(paiementService)
    val enseignantRoutes      = new EnseignantRoutes(enseignantService)
    val matiereRoutes         = new MatiereRoutes(matiereService)
    val filiereRoutes         = new FiliereRoutes(filiereService)
    val inscriptionRoutes     = new InscriptionRoutes(inscriptionService)
    val emploiDuTempsRoutes   = new EmploiDuTempsRoutes(emploiDuTempsService)
    val salleRoutes           = new SalleRoutes(salleService)
    val tableauDeBordRoutes   = new TableauDeBordRoutes(tableauDeBordService)

    val allRoutes = cors() {
      etudiantRoutes.routes      ~
        noteRoutes.routes          ~
        absenceRoutes.routes       ~
        paiementRoutes.routes      ~
        enseignantRoutes.routes    ~
        matiereRoutes.routes       ~
        filiereRoutes.routes       ~
        inscriptionRoutes.routes   ~
        emploiDuTempsRoutes.routes ~
        salleRoutes.routes         ~
        tableauDeBordRoutes.routes
    }

    Http().newServerAt("0.0.0.0", 8081).bind(allRoutes).onComplete {
      case Success(_) =>
        println("✅ Serveur démarré sur http://localhost:8081")
        println()
        println("📚 ÉTUDIANTS")
        println("   GET    /api/etudiants                                                → Lister tous les étudiants")
        println("   GET    /api/etudiants/{matricule}                                    → Détail d'un étudiant")
        println("   GET    /api/etudiants?filiere={filiere}                              → Filtrer par filière")
        println("   GET    /api/etudiants/suspendus                                      → Étudiants suspendus")
        println()
        println("📝 NOTES — CRUD")
        println("   POST   /api/notes                                                    → Enregistrer une note")
        println("   POST   /api/notes/bulk                                               → Enregistrer plusieurs notes en une fois")
        println("   GET    /api/notes/{noteId}                                           → Détail d'une note")
        println("   PUT    /api/notes/{noteId}                                           → Modifier une note")
        println("   DELETE /api/notes/{noteId}                                           → Supprimer une note")
        println()
        println("🔍 NOTES — CONSULTATION")
        println("   GET    /api/etudiants/{matricule}/notes                              → Toutes les notes d'un étudiant")
        println("   GET    /api/matieres/{matiereId}/notes                               → Toutes les notes d'une matière")
        println("   GET    /api/matieres/{matiereId}/notes/{matricule}                   → Note d'un étudiant dans une matière")
        println()
        println("📊 NOTES — CALCULS")
        println("   GET    /api/etudiants/{matricule}/notes/moyenne                      → Moyenne générale pondérée")
        println("   GET    /api/etudiants/{matricule}/matieres/{matiereId}/moyenne       → Moyenne matière (40% CC + 60% Examen)")
        println()
        println("📄 NOTES — DOCUMENTS")
        println("   GET    /api/etudiants/{matricule}/releve                             → Relevé de notes officiel")
        println("   GET    /api/etudiants/{matricule}/bilan                              → Bilan détaillé + rang")
        println()
        println("🛡️  NOTES — AUDIT & RÉSULTATS")
        println("   GET    /api/notes/audit/invalides                                    → Notes hors intervalle [0-20]")
        println("   GET    /api/etudiants/resultats/ajournes                             → Étudiants moyenne < 10")
        println("   GET    /api/etudiants/resultats/classement                           → Classement général par moyenne")
        println()
        println("🚨 ABSENCES")
        println("   POST   /api/absences                                                 → Enregistrer une absence")
        println("   POST   /api/absences/bulk                                            → Enregistrer plusieurs absences")
        println("   GET    /api/absences/{idAbsence}                                     → Détail d'une absence")
        println("   PUT    /api/absences/{idAbsence}                                     → Modifier une absence")
        println("   DELETE /api/absences/{idAbsence}                                     → Supprimer une absence")
        println("   GET    /api/etudiants/{matricule}/absences                           → Résumé absences d'un étudiant")
        println("   GET    /api/etudiants/{matricule}/absences/total                     → Total heures d'absence")
        println("   GET    /api/etudiants/{matricule}/absences/non-justifiees            → Absences non justifiées d'un étudiant")
        println("   GET    /api/absences/non-justifiees                                  → Toutes les absences non justifiées")
        println("   GET    /api/absences/alertes                                         → Étudiants > 10h d'absence")
        println("   GET    /api/absences/statistiques/filieres                           → Taux absentéisme par filière")
        println()
        println("💰 PAIEMENTS — CRUD")
        println("   POST   /api/paiements                                                → Enregistrer un paiement")
        println("   GET    /api/paiements                                                → Lister tous les paiements")
        println("   GET    /api/paiements/{idPaiement}                                   → Détail d'un paiement")
        println("   PUT    /api/paiements/{idPaiement}                                   → Modifier un paiement")
        println("   DELETE /api/paiements/{idPaiement}                                   → Supprimer un paiement")
        println()
        println("💰 PAIEMENTS — CONSULTATION")
        println("   GET    /api/etudiants/{matricule}/paiements                          → Paiements d'un étudiant")
        println("   GET    /api/etudiants/{matricule}/paiements/resume                   → Résumé financier d'un étudiant")
        println()
        println("💰 PAIEMENTS — STATISTIQUES")
        println("   GET    /api/paiements/stats                                          → Statistiques globales")
        println("   GET    /api/paiements/dettes                                         → Étudiants en dette")
        println()
        println("👨‍🏫 ENSEIGNANTS")
        println("   POST   /api/enseignants                                              → Créer un enseignant")
        println("   GET    /api/enseignants                                              → Lister tous les enseignants")
        println("   GET    /api/enseignants/{idEnseignant}                               → Détail d'un enseignant")
        println("   PUT    /api/enseignants/{idEnseignant}                               → Modifier un enseignant")
        println("   DELETE /api/enseignants/{idEnseignant}                               → Supprimer un enseignant")
        println()
        println("📘 MATIÈRES")
        println("   POST   /api/matieres                                                 → Créer une matière")
        println("   GET    /api/matieres                                                 → Lister toutes les matières")
        println("   GET    /api/matieres/{id}                                            → Détail d'une matière")
        println("   PUT    /api/matieres/{id}                                            → Modifier une matière")
        println("   DELETE /api/matieres/{id}                                            → Supprimer une matière")
        println("   GET    /api/enseignants/{id}/matieres                                → Matières d'un enseignant")
        println()
        println("🏫 FILIÈRES")
        println("   POST   /api/filieres                                                 → Créer une filière")
        println("   GET    /api/filieres                                                 → Lister toutes les filières")
        println("   GET    /api/filieres/{id}                                            → Détail d'une filière")
        println("   PUT    /api/filieres/{id}                                            → Modifier une filière")
        println("   DELETE /api/filieres/{id}                                            → Supprimer une filière")
        println()
        println("📋 INSCRIPTIONS")
        println("   POST   /api/inscriptions                                             → Créer une inscription")
        println("   GET    /api/inscriptions                                             → Lister toutes les inscriptions")
        println("   GET    /api/inscriptions/{id}                                        → Détail d'une inscription")
        println("   PUT    /api/inscriptions/{id}                                        → Modifier une inscription")
        println("   DELETE /api/inscriptions/{id}                                        → Supprimer une inscription")
        println("   GET    /api/inscriptions/matricule/{matricule}                       → Inscriptions par matricule")
        println("   GET    /api/inscriptions/filiere/{filiere}                           → Inscriptions par filière")
        println()
        println("🗓️  EMPLOI DU TEMPS")
        println("   POST   /api/emplois-du-temps                                         → Créer une séance")
        println("   GET    /api/emplois-du-temps                                         → Lister toutes les séances")
        println("   GET    /api/emplois-du-temps/{id}                                    → Détail d'une séance")
        println("   PUT    /api/emplois-du-temps/{id}                                    → Modifier une séance")
        println("   DELETE /api/emplois-du-temps/{id}                                    → Supprimer une séance")
        println("   GET    /api/emplois-du-temps/filiere/{filiere}                       → Emploi du temps par filière")
        println("   GET    /api/emplois-du-temps/niveau/{niveau}                         → Emploi du temps par niveau")
        println("   GET    /api/emplois-du-temps/enseignant/{enseignant}                 → Emploi du temps par enseignant")
        println("   GET    /api/emplois-du-temps/salle/{salle}                           → Emploi du temps par salle")
        println("   GET    /api/emplois-du-temps/classe/{filiere}/{niveau}               → Emploi du temps par classe")
        println()
        println("🏢 SALLES")
        println("   POST   /api/salles                                                   → Créer une salle")
        println("   GET    /api/salles                                                   → Lister toutes les salles")
        println("   GET    /api/salles/{id}                                              → Détail d'une salle")
        println("   PUT    /api/salles/{id}                                              → Modifier une salle")
        println("   DELETE /api/salles/{id}                                              → Supprimer une salle")
        println("   GET    /api/salles/type/{type}                                       → Salles par type")
        println()
        println("📊 TABLEAU DE BORD")
        println("   GET    /api/dashboard/etudiants/stats                                → Nombre total et par filière")
        println("   GET    /api/dashboard/reussite/filieres                              → Taux de réussite par filière")
        println("   GET    /api/dashboard/moyennes/niveaux                               → Moyenne générale par niveau")
        println("   GET    /api/dashboard/absenteisme                                    → Taux d'absentéisme global et par filière")
        println("   GET    /api/dashboard/classement                                     → Classement général des étudiants")
        println("   GET    /api/dashboard/matieres/faibles?limit=5                       → Matières aux plus faibles moyennes")
        println("   GET    /api/dashboard/enseignants/volume-horaire?limit=5             → Enseignants par volume horaire")
        println("   GET    /api/dashboard/etudiants/risque                               → Étudiants à risque académique")

      case Failure(ex) =>
        println(s"❌ Échec du démarrage : ${ex.getMessage}")
        system.terminate()
    }

    sys.addShutdownHook {
      DatabaseConnection.shutdown()
      system.terminate()
    }
  }
}