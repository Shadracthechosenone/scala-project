package universite.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import universite.model.Inscription
import spray.json._
import universite.service.InscriptionService

trait InscriptionJsonProtocol extends DefaultJsonProtocol {
  implicit val inscriptionFormat: RootJsonFormat[Inscription] = jsonFormat6(Inscription.apply)
}

class InscriptionRoutes(service: InscriptionService) extends InscriptionJsonProtocol {

  val routes = pathPrefix("api" / "inscriptions") {
    pathEndOrSingleSlash {
      get  { complete(service.getAll()) } ~
        post { entity(as[Inscription]) { i =>
          service.create(i) match {
            case Right(ins) => complete(ins)
            case Left(err)  => complete(400 -> err)
          }
        }}
    } ~
      path(Segment) { id =>
        get {
          service.getById(id) match {
            case Right(i)  => complete(i)
            case Left(err) => complete(404 -> err)
          }
        } ~
          put { entity(as[Inscription]) { i =>
            service.update(i) match {
              case Right(ins) => complete(ins)
              case Left(err)  => complete(400 -> err)
            }
          }} ~
          delete {
            service.delete(id) match {
              case Right(_)  => complete(200 -> "Supprimée")
              case Left(err) => complete(404 -> err)
            }
          }
      } ~
      path("matricule" / Segment) { m =>
        get { complete(service.getByMatricule(m)) }
      } ~
      path("filiere" / Segment) { f =>
        get { complete(service.getByFiliere(f)) }
      }
  }
}