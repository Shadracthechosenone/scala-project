package universite.bigdata

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

class Indicators(
                  spark: SparkSession,
                  etudiants:    DataFrame,
                  notes:        DataFrame,
                  absences:     DataFrame,
                  paiements:    DataFrame,
                  inscriptions: DataFrame,
                  matieres:     DataFrame
                ) {

  // Moyenne matière = 40% CC + 60% examen
  private val avecMoyenne = notes.withColumn(
    "moyenne",
    col("controle_continu") * 0.4 + col("examen") * 0.6
  )

  def totalEtudiants(): DataFrame =
    spark.createDataFrame(
      Seq(("total_etudiants", etudiants.count().toString))
    ).toDF("indicateur", "valeur")

  def etudiantsParFiliere(): DataFrame =
    etudiants.groupBy("filiere").agg(count("*").alias("nombre"))

  def etudiantsParNiveau(): DataFrame =
    etudiants.groupBy("niveau").agg(count("*").alias("nombre"))

  def moyenneParFiliere(): DataFrame = {
    val avecFiliere = avecMoyenne.join(
      inscriptions.select("matricule", "filiere"), Seq("matricule"), "left"
    )
    avecFiliere.groupBy("filiere")
      .agg(round(avg("moyenne"), 2).alias("moyenne_generale"))
      .orderBy(desc("moyenne_generale"))
  }

  def top5Etudiants(): DataFrame =
    avecMoyenne
      .groupBy("matricule")
      .agg(round(avg("moyenne"), 2).alias("moyenne"))
      .join(etudiants.select("matricule", "nom", "prenom", "filiere"), Seq("matricule"), "left")
      .orderBy(desc("moyenne"))
      .limit(5)

  def etudiantsARisque(): DataFrame = {
    val moyennes = avecMoyenne.groupBy("matricule")
      .agg(round(avg("moyenne"), 2).alias("moyenne"))

    val totalAbsences = absences.groupBy("matricule")
      .agg(sum("heures").alias("total_heures"))

    moyennes.join(totalAbsences, Seq("matricule"), "left")
      .filter(col("moyenne") < 10 || col("total_heures") > 10)
      .join(etudiants.select("matricule", "nom", "prenom", "filiere"), Seq("matricule"), "left")
      .select("matricule", "nom", "prenom", "filiere", "moyenne", "total_heures")
  }

  def tauxAbsenteismeGlobal(): DataFrame = {
    val totalHeures  = absences.agg(sum("heures").alias("total_heures_absence")).first().getLong(0)
    val nbEtudiants  = etudiants.count()
    val taux         = if (nbEtudiants > 0) totalHeures.toDouble / nbEtudiants else 0.0

    spark.createDataFrame(Seq(
      ("total_heures_absence", totalHeures.toString),
      ("nb_etudiants",         nbEtudiants.toString),
      ("moyenne_heures_par_etudiant", f"$taux%.2f")
    )).toDF("indicateur", "valeur")
  }

  def absenteismeParMatiere(): DataFrame =
    absences.groupBy("matiere")
      .agg(
        sum("heures").alias("total_heures"),
        count("*").alias("nb_absences")
      )
      .join(matieres.select("id_matiere", "nom_matiere"),
        absences("matiere") === matieres("id_matiere"), "left")
      .select("matiere", "nom_matiere", "total_heures", "nb_absences")
      .orderBy(desc("total_heures"))

  def statsPaiements(): DataFrame = {
    val totalAttendu   = paiements.agg(sum("montant_total").alias("v")).first().getLong(0)
    val totalEncaisse  = paiements.agg(sum("montant_paye").alias("v")).first().getLong(0)
    val restant        = totalAttendu - totalEncaisse

    spark.createDataFrame(Seq(
      ("montant_total_attendu",   totalAttendu.toString),
      ("montant_total_encaisse",  totalEncaisse.toString),
      ("montant_restant",         restant.toString),
      ("taux_recouvrement",       f"${totalEncaisse * 100.0 / totalAttendu}%.2f%%")
    )).toDF("indicateur", "valeur")
  }

  def tauxReussiteParFiliere(): DataFrame = {
    val avecFiliere = avecMoyenne
      .join(inscriptions.select("matricule", "filiere"), Seq("matricule"), "left")

    val total   = avecFiliere.groupBy("filiere").agg(count("*").alias("total"))
    val admis   = avecFiliere.filter(col("moyenne") >= 10)
      .groupBy("filiere").agg(count("*").alias("admis"))

    total.join(admis, Seq("filiere"), "left")
      .withColumn("taux_reussite", round(col("admis") * 100.0 / col("total"), 2))
      .orderBy(desc("taux_reussite"))
  }

  def tauxReussiteGlobal(): DataFrame = {
    val total = avecMoyenne.count()
    val admis = avecMoyenne.filter(col("moyenne") >= 10).count()
    val taux  = if (total > 0) admis * 100.0 / total else 0.0

    spark.createDataFrame(Seq(
      ("total_notes",    total.toString),
      ("admis",          admis.toString),
      ("taux_reussite",  f"$taux%.2f%%")
    )).toDF("indicateur", "valeur")
  }

  def matierePlusDifficile(): DataFrame =
    avecMoyenne.groupBy("matiere")
      .agg(round(avg("moyenne"), 2).alias("moyenne"))
      .join(matieres.select("id_matiere", "nom_matiere"),
        avecMoyenne("matiere") === matieres("id_matiere"), "left")
      .orderBy(asc("moyenne"))
      .limit(1)

  def meilleureFiliere(): DataFrame =
    tauxReussiteParFiliere().orderBy(desc("taux_reussite")).limit(1)
}