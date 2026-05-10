package universite.bigdata

import org.apache.spark.sql.{DataFrame, SparkSession}
import scala.util.{Try, Success, Failure}

class CsvLoader(spark: SparkSession) {

  def load(path: String): DataFrame =
    Try {
      spark.read
        .option("header", "true")
        .option("inferSchema", "true")
        .option("encoding", "UTF-8")
        .csv(path)
    } match {
      case Success(df) =>
        println(s"✅ Chargé : $path (${df.count()} lignes)")
        df
      case Failure(e)  =>
        println(s"❌ Erreur chargement $path : ${e.getMessage}")
        spark.emptyDataFrame
    }
}