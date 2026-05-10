package universite.bigdata

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import scala.util.{Failure, Success, Try}

class ResultExporter {

  def save(df: DataFrame, path: String, format: String = "csv"): Unit =
    Try {
      format match {
        case "parquet" =>
          df.coalesce(1).write.mode("overwrite").parquet(path)
        case _ =>
          df.coalesce(1).write.mode("overwrite")
            .option("header", "true")
            .csv(path)
      }
    } match {
      case Success(_) => println(s"✅ Sauvegardé : $path ($format)")
      case Failure(e) => println(s"❌ Erreur export $path : ${e.getMessage}")
    }

  def saveParquet(df: DataFrame, path: String): Unit =
    save(df, path, "parquet")
}