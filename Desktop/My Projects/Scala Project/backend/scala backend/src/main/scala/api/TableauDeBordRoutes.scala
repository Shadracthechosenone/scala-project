package universite.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import universite.model._
import universite.service.TableauDeBordService
import spray.json._

trait TableauDeBordJsonProtocol extends DefaultJsonProtocol {
  implicit val statEtudiantsFormat          : RootJsonFormat[StatEtudiants]              = jsonFormat2(StatEtudiants.apply)
  implicit val statReussiteFormat           : RootJsonFormat[StatReussite]               = jsonFormat2(StatReussite.apply)
  implicit val statMoyenneNiveauFormat      : RootJsonFormat[StatMoyenneNiveau]          = jsonFormat2(StatMoyenneNiveau.apply)
  implicit val statAbsenteismeFormat        : RootJsonFormat[StatAbsenteisme]            = jsonFormat2(StatAbsenteisme.apply)
  implicit val classementEtudiantFormat     : RootJsonFormat[ClassementEtudiant]         = jsonFormat6(ClassementEtudiant.apply)
  implicit val statMatiereFaibleFormat      : RootJsonFormat[StatMatiereFaible]          = jsonFormat3(StatMatiereFaible.apply)
  implicit val statVolHoraireEnseignantFormat: RootJsonFormat[StatVolHoraireEnseignant]  = jsonFormat3(StatVolHoraireEnseignant.apply)
  implicit val etudiantRisqueFormat         : RootJsonFormat[EtudiantRisque]             = jsonFormat7(EtudiantRisque.apply)
}

class TableauDeBordRoutes(service: TableauDeBordService) extends TableauDeBordJsonProtocol {

  val routes = pathPrefix("api" / "dashboard") {

    path("etudiants" / "stats") {
      get { complete(service.getStatEtudiants()) }
    }

    path("reussite" / "filieres") {
      get { complete(service.getTauxReussiteParFiliere()) }
    }

    path("moyennes" / "niveaux") {
      get { complete(service.getMoyenneParNiveau()) }
    }

    path("absenteisme") {
      get { complete(service.getTauxAbsenteisme()) }
    }

    path("classement") {
      get { complete(service.getClassement()) }
    }

    path("matieres" / "faibles") {
      get {
        parameter("limit".as[Int].withDefault(5)) { limit =>
          complete(service.getMatieresFaibles(limit))
        }
      }
    }

    path("enseignants" / "volume-horaire") {
      get {
        parameter("limit".as[Int].withDefault(5)) { limit =>
          complete(service.getEnseignantsVolHoraire(limit))
        }
      }
    }

    path("etudiants" / "risque") {
      get { complete(service.getEtudiantsARisque()) }
    }
  }
}