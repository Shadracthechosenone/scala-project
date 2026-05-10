// routes/PaiementRoutes.scala
package universite.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import model.Paiement
import spray.json.RootJsonWriter
import universite.service.PaiementService

import scala.util.{Failure, Success}

class PaiementRoutes(paiementService: PaiementService) extends PaiementJsonProtocol {

  private def ok[A: RootJsonWriter](data: A): Route =
    complete(StatusCodes.OK, data)

  private def created[A: RootJsonWriter](data: A): Route =
    complete(StatusCodes.Created, data)

  private def notFound(msg: String): Route =
    complete(StatusCodes.NotFound, PaiementErrorResponse(success = false, error = msg))

  private def badRequest(msg: String): Route =
    complete(StatusCodes.BadRequest, PaiementErrorResponse(success = false, error = msg))

  private def serverError(msg: String): Route =
    complete(StatusCodes.InternalServerError, PaiementErrorResponse(success = false, error = msg))

  val routes: Route = concat(

    // POST /api/paiements
    (post & path("api" / "paiements") & pathEndOrSingleSlash & entity(as[Paiement])) { paiement =>
      paiementService.enregistrerPaiement(paiement) match {
        case Success(p)                           => created(p)
        case Failure(e: IllegalArgumentException) => badRequest(e.getMessage)
        case Failure(e)                           => serverError(e.getMessage)
      }
    },

    // GET /api/paiements/{idPaiement}
    (get & path("api" / "paiements" / Segment)) { idPaiement =>
      paiementService.chercherPaiement(idPaiement) match {
        case Some(p) => ok(p)
        case None    => notFound(s"Paiement $idPaiement introuvable.")
      }
    },

    // PUT /api/paiements/{idPaiement}
    (put & path("api" / "paiements" / Segment) & entity(as[Paiement])) { (idPaiement, paiement) =>
      paiementService.modifierPaiement(paiement.copy(idPaiement = idPaiement)) match {
        case Success(p)                           => ok(p)
        case Failure(e: IllegalArgumentException) => badRequest(e.getMessage)
        case Failure(e: NoSuchElementException)   => notFound(e.getMessage)
        case Failure(e)                           => serverError(e.getMessage)
      }
    },

    // DELETE /api/paiements/{idPaiement}
    (delete & path("api" / "paiements" / Segment)) { idPaiement =>
      paiementService.supprimerPaiement(idPaiement) match {
        case Success(_)                         => ok(PaiementMessageResponse(s"Paiement $idPaiement supprimé."))
        case Failure(e: NoSuchElementException) => notFound(e.getMessage)
        case Failure(e)                         => serverError(e.getMessage)
      }
    },

    // GET /api/etudiants/{matricule}/paiements
    (get & path("api" / "etudiants" / Segment / "paiements")) { matricule =>
      ok(paiementService.resumePaiementsEtudiant(matricule))
    },

    // GET /api/paiements/dettes — étudiants ayant une dette
    (get & path("api" / "paiements" / "dettes")) {
      val dettes = paiementService.etudiantsEnDette()
      ok(DettesResponse(count = dettes.size, etudiants = dettes))
    },

    // GET /api/paiements/encaisse — montant total encaissé
    (get & path("api" / "paiements" / "encaisse")) {
      ok(Map("montantTotalEncaisse" -> paiementService.montantTotalEncaisse()))
    },

    // GET /api/paiements/statistiques — taux de recouvrement + stats globales
    (get & path("api" / "paiements" / "statistiques")) {
      ok(paiementService.statistiquesGlobales())
    }
  )
}