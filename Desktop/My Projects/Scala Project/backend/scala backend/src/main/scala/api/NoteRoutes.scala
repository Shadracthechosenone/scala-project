package universite.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._
import universite.model._
import universite.service.NoteService

import scala.util.{Failure, Success}

class NoteRoutes(noteService: NoteService) extends NoteJsonProtocol {

  // ── Helpers réponse ──────────────────────────────────────────────────────
  // On passe le marshaller implicite explicitement via [A: RootJsonWriter]
  // et on utilise complete(StatusCode, value) — jamais complete(code -> value)

  private def ok[A: RootJsonWriter](data: A): Route =
    complete(StatusCodes.OK, data)

  private def created[A: RootJsonWriter](data: A): Route =
    complete(StatusCodes.Created, data)

  private def notFound(msg: String): Route =
    complete(StatusCodes.NotFound, ErrorResponse(success = false, error = msg))

  private def badRequest(msg: String): Route =
    complete(StatusCodes.BadRequest, ErrorResponse(success = false, error = msg))

  private def serverError(msg: String): Route =
    complete(StatusCodes.InternalServerError, ErrorResponse(success = false, error = msg))

  // ── Routes ───────────────────────────────────────────────────────────────

  val routes: Route = pathPrefix("api" / "notes") {
    concat(

      // POST /api/notes — créer une note
      (post & pathEndOrSingleSlash & entity(as[Note])) { note =>
        noteService.creerNote(note) match {
          case Success(n)                           => created(n)
          case Failure(e: IllegalArgumentException) => badRequest(e.getMessage)
          case Failure(e: NoSuchElementException)   => notFound(e.getMessage)
          case Failure(e)                           => serverError(e.getMessage)
        }
      },

      // POST /api/notes/bulk — enregistrer plusieurs notes en une seule requête
      (post & path("bulk") & entity(as[List[Note]])) { notes =>
        val resultats = noteService.creerNotes(notes)
        val succes    = resultats.collect { case Right(n) => n }
        val echecs    = resultats.collect { case Left(e)  => e  }
        ok(BulkNoteResponse(
          succes   = succes,
          echecs   = echecs,
          total    = notes.size,
          reussis  = succes.size,
          echoues  = echecs.size
        ))
      },

      // GET /api/notes/invalides — notes hors [0-20]
      (get & path("invalides")) {
        ok(NotesListResponse(count = noteService.notesInvalides().size, notes = noteService.notesInvalides()))
      },

      // GET /api/notes/ajournes — étudiants moyenne < 10
      (get & path("ajournes")) {
        val ajournes = noteService.etudiantsAjournes()
        ok(AjournesResponse(
          count     = ajournes.size,
          etudiants = ajournes.map { case (mat, moy) => AjourneItem(mat, moy) }
        ))
      },

      // GET /api/notes/classement — classement général décroissant
      (get & path("classement")) {
        val classement = noteService.classerEtudiants()
        ok(ClassementResponse(
          classement = classement.map { case (rang, mat, nom, moy) =>
            ClassementItem(rang, mat, nom, moy)
          }
        ))
      },

      // GET /api/notes/etudiant/:matricule — toutes les notes d'un étudiant
      (get & path("etudiant" / Segment)) { matricule =>
        ok(noteService.notesParEtudiant(matricule))
      },

      // GET /api/notes/matiere/:idMatiere — notes d'une matière
      (get & path("matiere" / Segment)) { idMatiere =>
        ok(noteService.notesParMatiere(idMatiere))
      },

      // GET /api/notes/moyenne/matiere/:matricule/:idMatiere — 40% CC + 60% Examen
      (get & path("moyenne" / "matiere" / Segment / Segment)) { (matricule, idMatiere) =>
        noteService.moyenneMatiere(matricule, idMatiere) match {
          case Some(moy) => ok(MoyenneMatiereResponse(matricule, idMatiere, moy))
          case None      => notFound(s"Aucune note pour $matricule / $idMatiere.")
        }
      },

      // GET /api/notes/moyenne/generale/:matricule — moyenne générale pondérée
      (get & path("moyenne" / "generale" / Segment)) { matricule =>
        noteService.moyenneGenerale(matricule) match {
          case Some(moy) => ok(MoyenneGeneraleResponse(matricule, moy))
          case None      => notFound(s"Impossible de calculer la moyenne de $matricule.")
        }
      },

      // GET /api/notes/releve/:matricule — relevé complet + mention + décision
      (get & path("releve" / Segment)) { matricule =>
        noteService.releveNotes(matricule) match {
          case Some(releve) => ok(releve)
          case None         => notFound(s"Etudiant $matricule introuvable.")
        }
      },

      // GET /api/notes/bilan/:matricule — bilan détaillé + rang optionnel
      (get & path("bilan" / Segment)) { matricule =>
        noteService.bilanEtudiant(matricule) match {
          case Some(bilan) => ok(bilan)
          case None        => notFound(s"Etudiant $matricule introuvable.")
        }
      },

      // GET /api/notes/:id — chercher une note par ID
      // ⚠️  Ce segment générique doit rester EN DERNIER pour ne pas capturer
      //     les chemins statiques ("invalides", "ajournes", etc.) définis au-dessus
      (get & path(Segment)) { idNote =>
        noteService.chercherNote(idNote) match {
          case Some(note) => ok(note)
          case None       => notFound(s"Note $idNote introuvable.")
        }
      },

      // PUT /api/notes/:id — modifier une note
      (put & path(Segment) & entity(as[Note])) { (idNote, note) =>
        noteService.modifierNote(note.copy(idNote = idNote)) match {
          case Success(n)                           => ok(n)
          case Failure(e: IllegalArgumentException) => badRequest(e.getMessage)
          case Failure(e: NoSuchElementException)   => notFound(e.getMessage)
          case Failure(e)                           => serverError(e.getMessage)
        }
      },

      // DELETE /api/notes/:id — supprimer une note
      (delete & path(Segment)) { idNote =>
        noteService.supprimerNote(idNote) match {
          case Success(_)                         => ok(MessageResponse(s"Note $idNote supprimée."))
          case Failure(e: NoSuchElementException) => notFound(e.getMessage)
          case Failure(e)                         => serverError(e.getMessage)
        }
      }
    )
  }
}