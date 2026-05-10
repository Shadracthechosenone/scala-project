package traits

trait Calculable {
  def calculer(): Double
  def getResultFormatted(): String = f"${calculer()}%.2f"
}