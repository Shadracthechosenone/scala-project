package universite.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import universite.model.Seance
import universite.service.EmploiDuTempsService
import spray.json._

trait SeanceJsonProtocol extends DefaultJsonProtocol {
  implicit val seanceFormat: RootJsonFormat[Seance] = jsonFormat9(Seance.apply)
}

class EmploiDuTempsRoutes(service: EmploiDuTempsService) extends SeanceJsonProtocol {

  val routes = pathPrefix("api" / "emplois-du-temps") {

    pathEndOrSingleSlash {
      get  { complete(service.getAll()) } ~
        post { entity(as[Seance]) { s =>
          service.create(s) match {
            case Right(seance) => complete(seance)
            case Left(err)     => complete(400 -> err)
          }
        }}
    }

    path(Segment) { id =>
      get {
        service.getById(id) match {
          case Right(s)  => complete(s)
          case Left(err) => complete(404 -> err)
        }
      }
        put { entity(as[Seance]) { s =>
          service.update(s) match {
            case Right(seance) => complete(seance)
            case Left(err)     => complete(400 -> err)
          }
        }} ~
        delete {
          service.delete(id) match {
            case Right(_)  => complete(200 -> "Séance supprimée")
            case Left(err) => complete(404 -> err)
          }
        }
    }

    path("filiere" / Segment) { f =>
      get { complete(service.getByFiliere(f)) }
    }

    path("niveau" / Segment) { n =>
      get { complete(service.getByNiveau(n)) }
    }

    path("enseignant" / Segment) { e =>
      get { complete(service.getByEnseignant(e)) }
    }

    path("salle" / Segment) { s =>
      get { complete(service.getBySalle(s)) }
    }

    path("classe" / Segment / Segment) { (f, n) =>
      get { complete(service.getByFiliereAndNiveau(f, n)) }
    }
  }
}