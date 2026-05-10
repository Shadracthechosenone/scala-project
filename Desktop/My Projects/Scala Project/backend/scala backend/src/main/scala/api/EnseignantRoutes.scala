package universite.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import model.Enseignant
import spray.json._
import universite.service.EnseignantService

trait EnseignantJsonProtocol extends DefaultJsonProtocol {
  implicit val enseignantFormat: RootJsonFormat[Enseignant] = jsonFormat8(Enseignant.apply)
}

class EnseignantRoutes(service: EnseignantService) extends EnseignantJsonProtocol {

  val routes = pathPrefix("api" / "enseignants") {
    pathEndOrSingleSlash {
      get  { complete(service.tous()) } ~
        post { entity(as[Enseignant]) { e =>
          service.creer(e) match {
            case Right(en) => complete(en)
            case Left(err) => complete(400 -> err)
          }
        }}
    } ~
      path(Segment) { id =>
        get {
          service.trouverParId(id) match {
            case Right(e)  => complete(e)
            case Left(err) => complete(404 -> err)
          }
        } ~
          put { entity(as[Enseignant]) { e =>
            service.modifier(e) match {
              case Right(en) => complete(en)
              case Left(err) => complete(400 -> err)
            }
          }} ~
          delete {
            service.supprimer(id) match {
              case Right(_)  => complete(200 -> "Supprimé")
              case Left(err) => complete(404 -> err)
            }
          }
      }
  }
}