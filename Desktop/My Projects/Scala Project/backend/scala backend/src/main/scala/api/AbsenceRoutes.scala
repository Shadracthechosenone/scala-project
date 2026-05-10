package universite.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import model.Absence
import spray.json.RootJsonWriter
import universite.model._
import universite.service.AbsenceService

import scala.util.{Failure, Success}

class AbsenceRoutes(absenceService: AbsenceService) extends AbsenceJsonProtocol {

  private def ok[A: RootJsonWriter](data: A): Route =
    complete(StatusCodes.OK, data)

  private def created[A: RootJsonWriter](data: A): Route =
    complete(StatusCodes.Created, data)

  private def notFound(msg: String): Route =
    complete(StatusCodes.NotFound, AbsenceErrorResponse(success = false, error = msg))

  private def badRequest(msg: String): Route =
    complete(StatusCodes.BadRequest, AbsenceErrorResponse(success = false, error = msg))

  private def serverError(msg: String): Route =
    complete(StatusCodes.InternalServerError, AbsenceErrorResponse(success = false, error = msg))

  val routes: Route = concat(

    // ── CRUD ──────────────────────────────────────────────────────────────

    // POST /api/absences
    (post & path("api" / "absences") & pathEndOrSingleSlash & entity(as[Absence])) { absence =>
      absenceService.enregistrerAbsence(absence) match {
        case Success(a)                           => created(a)
        case Failure(e: IllegalArgumentException) => badRequest(e.getMessage)
        case Failure(e: NoSuchElementException)   => notFound(e.getMessage)
        case Failure(e)                           => serverError(e.getMessage)
      }
    },

    // POST /api/absences/bulk
    (post & path("api" / "absences" / "bulk") & entity(as[List[Absence]])) { absences =>
      val resultats = absenceService.enregistrerAbsences(absences)
      val succes    = resultats.collect { case Right(a) => a }
      val echecs    = resultats.collect { case Left(e)  => e }
      ok(BulkAbsenceResponse(succes, echecs, absences.size, succes.size, echecs.size))
    },

    // GET /api/absences/{idAbsence}
    (get & path("api" / "absences" / Segment)) { idAbsence =>
      absenceService.chercherAbsence(idAbsence) match {
        case Some(a) => ok(a)
        case None    => notFound(s"Absence $idAbsence introuvable.")
      }
    },

    // PUT /api/absences/{idAbsence}
    (put & path("api" / "absences" / Segment) & entity(as[Absence])) { (idAbsence, absence) =>
      absenceService.modifierAbsence(absence.copy(idAbsence = idAbsence)) match {
        case Success(a)                           => ok(a)
        case Failure(e: IllegalArgumentException) => badRequest(e.getMessage)
        case Failure(e: NoSuchElementException)   => notFound(e.getMessage)
        case Failure(e)                           => serverError(e.getMessage)
      }
    },

    // DELETE /api/absences/{idAbsence}
    (delete & path("api" / "absences" / Segment)) { idAbsence =>
      absenceService.supprimerAbsence(idAbsence) match {
        case Success(_)                         => ok(AbsenceMessageResponse(s"Absence $idAbsence supprimée."))
        case Failure(e: NoSuchElementException) => notFound(e.getMessage)
        case Failure(e)                         => serverError(e.getMessage)
      }
    },

    // ── CONSULTATION ──────────────────────────────────────────────────────

    // GET /api/etudiants/{matricule}/absences
    (get & path("api" / "etudiants" / Segment / "absences")) { matricule =>
      absenceService.resumeAbsencesEtudiant(matricule) match {
        case Some(resume) => ok(resume)
        case None         => notFound(s"Étudiant $matricule introuvable.")
      }
    },

    // GET /api/absences/non-justifiees
    (get & path("api" / "absences" / "non-justifiees")) {
      ok(absenceService.absencesNonJustifiees())
    },

    // GET /api/etudiants/{matricule}/absences/non-justifiees
    (get & path("api" / "etudiants" / Segment / "absences" / "non-justifiees")) { matricule =>
      ok(absenceService.absencesNonJustifieesParEtudiant(matricule))
    },

    // ── CALCULS & ALERTES ─────────────────────────────────────────────────

    // GET /api/etudiants/{matricule}/absences/total
    (get & path("api" / "etudiants" / Segment / "absences" / "total")) { matricule =>
      ok(TotalHeuresResponse(matricule, absenceService.totalHeuresAbsence(matricule)))
    },

    // GET /api/absences/alertes — étudiants > 10h
    (get & path("api" / "absences" / "alertes")) {
      val alertes = absenceService.etudiantsEnAlerte()
      ok(AlertesResponse(count = alertes.size, etudiants = alertes))
    },

    // GET /api/absences/statistiques/filieres — taux absentéisme par filière
    (get & path("api" / "absences" / "statistiques" / "filieres")) {
      ok(absenceService.tauxAbsenteismeParFiliere())
    }
  )
}