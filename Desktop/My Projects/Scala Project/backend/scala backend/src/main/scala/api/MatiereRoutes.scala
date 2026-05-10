package universite.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import universite.model.Matiere
import spray.json._
import universite.service.MatiereService

trait MatiereJsonProtocol extends DefaultJsonProtocol {
  implicit val matiereFormat: RootJsonFormat[Matiere] = jsonFormat6(Matiere.apply)
}

class MatiereRoutes(service: MatiereService) extends MatiereJsonProtocol {

  val routes = pathPrefix("api" / "matieres") {
    pathEndOrSingleSlash {
      get  { complete(service.toutes()) } ~
        post { entity(as[Matiere]) { m =>
          service.creer(m) match {
            case Right(mat) => complete(mat)
            case Left(err)  => complete(400 -> err)
          }
        }}
    } ~
      path(Segment) { id =>
        get {
          service.trouverParId(id) match {
            case Right(m)  => complete(m)
            case Left(err) => complete(404 -> err)
          }
        } ~
          put { entity(as[Matiere]) { m =>
            service.modifier(m) match {
              case Right(mat) => complete(mat)
              case Left(err)  => complete(400 -> err)
            }
          }} ~
          delete {
            service.supprimer(id) match {
              case Right(_)  => complete(200 -> "Supprimée")
              case Left(err) => complete(404 -> err)
            }
          }
      } ~
      path(Segment / "matieres") { idEnseignant =>
        get { complete(service.parEnseignant(idEnseignant)) }
      }
  }
}