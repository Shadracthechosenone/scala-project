package universite.bigdata

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

class DataCleaner {

  def clean(df: DataFrame): DataFrame = {
    val sansDoublons = df.dropDuplicates()

    // Détecter et afficher les valeurs manquantes
    println("🔍 Valeurs manquantes détectées :")
    sansDoublons.columns.foreach { col =>
      val missing = sansDoublons.filter(sansDoublons(col).isNull || sansDoublons(col) === "").count()
      if (missing > 0) println(s"   - $col : $missing valeur(s) manquante(s)")
    }

    // Supprimer les lignes avec valeurs nulles sur colonnes critiques
    sansDoublons.na.drop("any")
  }
}