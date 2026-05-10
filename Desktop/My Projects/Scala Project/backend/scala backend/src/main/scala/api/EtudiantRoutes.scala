package api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._
import universite.model.{Etudiant, StatutEtudiant}
import universite.service.EtudiantService
import EtudiantJsonProtocol._
import scala.language.postfixOps
import scala.util.{Failure, Success}

class EtudiantRoutes(service: EtudiantService) {


  private def jsonMsg(key: String, value: String): JsValue =
    JsObject(key -> JsString(value))

  val routes: Route =
    pathPrefix("api" / "etudiants") {
      concat(

        // GET /api/etudiants/stats/actifs/count
        path("stats" / "actifs" / "count") {
          get {
            complete(JsObject("count" -> JsNumber(service.compterActifs())))
          }
        },

        // GET /api/etudiants/suspendus
        path("suspendus") {
          get {
            complete(service.listerSuspendus().toJson)
          }
        },

        // GET /api/etudiants?filiere=X&niveau=Y&statut=Z
        // POST /api/etudiants
        pathEndOrSingleSlash {
          concat(
            get {
              parameters("filiere".?, "niveau".?, "statut".?) {
                (filiere, niveau, statut) =>
                  val result = (filiere, niveau, statut) match {
                    case (Some(f), _, _) => service.filtrerParFiliere(f)
                    case (_, Some(n), _) => service.filtrerParNiveau(n)
                    case (_, _, Some(s)) => service.filtrerParStatut(StatutEtudiant.fromString(s))
                    case _               => service.listerTous()
                  }
                  complete(result.toJson)
              }
            },
            post {
              entity(as[Etudiant]) { etudiant =>
                service.creerEtudiant(etudiant) match {
                  case Success(e)  => complete(StatusCodes.Created  -> e.toJson)
                  case Failure(ex) => complete(StatusCodes.Conflict -> jsonMsg("erreur", ex.getMessage))
                }
              }
            }
          )
        },

        // GET    /api/etudiants/{matricule}
        // PUT    /api/etudiants/{matricule}
        // DELETE /api/etudiants/{matricule}
        path(Segment) { matricule =>
          concat(
            get {
              service.rechercherParMatricule(matricule) match {
                case Some(e) => complete(e.toJson)
                case None    => complete(StatusCodes.NotFound -> jsonMsg("erreur", s"Etudiant $matricule introuvable"))
              }
            },
            put {
              entity(as[Etudiant]) { etudiant =>
                service.modifierEtudiant(etudiant.copy(matricule = matricule)) match {
                  case Success(e)  => complete(e.toJson)
                  case Failure(ex) => complete(StatusCodes.BadRequest -> jsonMsg("erreur", ex.getMessage))
                }
              }
            },
            delete {
              service.supprimerEtudiant(matricule) match {
                case Success(true)  => complete(StatusCodes.OK       -> jsonMsg("message", s"Etudiant $matricule supprime"))
                case Success(false) => complete(StatusCodes.NotFound -> jsonMsg("erreur",  s"Etudiant $matricule introuvable"))
                case Failure(ex)    => complete(StatusCodes.InternalServerError -> jsonMsg("erreur", ex.getMessage))
              }
            }
          )
        }

      ) // fin concat principal
    }
}