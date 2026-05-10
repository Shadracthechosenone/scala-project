package universite.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import universite.model.Filiere
import spray.json._
import universite.service.FiliereService

trait FiliereJsonProtocol extends DefaultJsonProtocol {
  implicit val filiereFormat: RootJsonFormat[Filiere] = jsonFormat3(Filiere.apply)
}

class FiliereRoutes(service: FiliereService) extends FiliereJsonProtocol {

  val routes = pathPrefix("api" / "filieres") {
    pathEndOrSingleSlash {
      get  { complete(service.getAll()) } ~
        post { entity(as[Filiere]) { f =>
          service.create(f) match {
            case Right(fil) => complete(fil)
            case Left(err)  => complete(400 -> err)
          }
        }}
    } ~
      path(Segment) { id =>
        get {
          service.getById(id) match {
            case Right(f)  => complete(f)
            case Left(err) => complete(404 -> err)
          }
        } ~
          put { entity(as[Filiere]) { f =>
            service.update(f) match {
              case Right(fil) => complete(fil)
              case Left(err)  => complete(400 -> err)
            }
          }} ~
          delete {
            service.delete(id) match {
              case Right(_)  => complete(200 -> "Supprimée")
              case Left(err) => complete(404 -> err)
            }
          }
      }
  }
}