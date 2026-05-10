package universite.bigdata

import org.apache.spark.sql.SparkSession
import java.io.File
import scala.util.{Failure, Success, Try}

object SparkApp {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("GestionUniversitaire")
      .master("local[*]")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    // Vérification existence des CSV
    val fichiers = List("etudiants", "notes", "absences", "paiements", "inscriptions", "matieres")
    fichiers.foreach { f =>
      val file = new File(s"data/$f.csv")
      println(s"${if (file.exists()) "✅" else "❌"} data/$f.csv — existe: ${file.exists()} — chemin: ${file.getAbsolutePath}")
    }

    val loader   = new CsvLoader(spark)
    val cleaner  = new DataCleaner()
    val exporter = new ResultExporter()

    Try {
      val etudiants    = cleaner.clean(loader.load("data/etudiants.csv"))
      val notes        = cleaner.clean(loader.load("data/notes.csv"))
      val absences     = cleaner.clean(loader.load("data/absences.csv"))
      val paiements    = cleaner.clean(loader.load("data/paiements.csv"))
      val inscriptions = cleaner.clean(loader.load("data/inscriptions.csv"))
      val matieres     = cleaner.clean(loader.load("data/matieres.csv"))

      val indicators = new Indicators(spark, etudiants, notes, absences, paiements, inscriptions, matieres)

      exporter.save(indicators.totalEtudiants(),         "output/statistiques/total_etudiants")
      exporter.save(indicators.etudiantsParFiliere(),    "output/statistiques/etudiants_par_filiere")
      exporter.save(indicators.etudiantsParNiveau(),     "output/statistiques/etudiants_par_niveau")
      exporter.save(indicators.moyenneParFiliere(),      "output/statistiques/moyenne_par_filiere")
      exporter.save(indicators.top5Etudiants(),          "output/rapports/top5_etudiants")
      exporter.save(indicators.etudiantsARisque(),       "output/rapports/etudiants_a_risque")
      exporter.save(indicators.tauxAbsenteismeGlobal(),  "output/statistiques/absenteisme_global")
      exporter.save(indicators.absenteismeParMatiere(),  "output/statistiques/absenteisme_par_matiere")
      exporter.save(indicators.statsPaiements(),         "output/statistiques/stats_paiements")
      exporter.save(indicators.tauxReussiteParFiliere(), "output/statistiques/reussite_par_filiere")
      exporter.save(indicators.tauxReussiteGlobal(),     "output/statistiques/reussite_globale")
      exporter.save(indicators.matierePlusDifficile(),   "output/rapports/matiere_difficile")
      exporter.save(indicators.meilleureFiliere(),       "output/rapports/meilleure_filiere")

    } match {
      case Success(_) =>
        println("✅ Tous les indicateurs générés avec succès")
        spark.stop()
      case Failure(e) =>
        println(s"❌ Erreur : ${e.getMessage}")
        e.printStackTrace()
        spark.stop()
    }
  }
}