package universite.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import universite.model.Salle
import universite.service.SalleService
import spray.json._

trait SalleJsonProtocol extends DefaultJsonProtocol {
  implicit val salleFormat: RootJsonFormat[Salle] = jsonFormat4(Salle.apply)
}

class SalleRoutes(service: SalleService) extends SalleJsonProtocol {

  val routes = pathPrefix("api" / "salles") {

    pathEndOrSingleSlash {
      get  { complete(service.getAll()) } ~
        post { entity(as[Salle]) { s =>
          service.create(s) match {
            case Right(salle) => complete(salle)
            case Left(err)    => complete(400 -> err)
          }
        }}
    }

    path(Segment) { id =>
      get {
        service.getById(id) match {
          case Right(s)  => complete(s)
          case Left(err) => complete(404 -> err)
        }
      } ~
        put { entity(as[Salle]) { s =>
          service.update(s) match {
            case Right(salle) => complete(salle)
            case Left(err)    => complete(400 -> err)
          }
        }} ~
        delete {
          service.delete(id) match {
            case Right(_)  => complete(200 -> "Salle supprimée")
            case Left(err) => complete(404 -> err)
          }
        }
    }

    path("type" / Segment) { t =>
      get { complete(service.getByType(t)) }
    }
  }
}