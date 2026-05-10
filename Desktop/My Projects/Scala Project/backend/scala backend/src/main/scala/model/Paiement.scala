package model
import scala.util.Try
import java.time.LocalDate
import traits.{Validable,Calculable}
case class Paiement(
                     idPaiement: String,
                     matricule: String,
                     montantTotal: Double,
                     montantPaye: Double,
                     datePaiement: Option[LocalDate],
                     mode: String
                   ) extends Calculable with Validable {

  override def calculer(): Double = montantTotal - montantPaye

  override def isValid: Boolean = {
    montantTotal > 0 && montantPaye >= 0 && montantPaye <= montantTotal
  }

  def resteAPayer: Double = calculer()
  def estEndette: Boolean = resteAPayer > 0
  def tauxRecouvrement: Double = if (montantTotal > 0) (montantPaye / montantTotal) * 100 else 0
  def estSolde   : Boolean   = resteAPayer <= 0
}

object Paiement {
  def fromCsvLine(line: String): Option[Paiement] = {
    Try {
      val parts = line.split(",")
      if (parts.length >= 6) {
        val datePaiement = if (parts(4).nonEmpty) Some(LocalDate.parse(parts(4))) else None
        Some(Paiement(
          idPaiement = parts(0),
          matricule = parts(1),
          montantTotal = parts(2).toDouble,
          montantPaye = parts(3).toDouble,
          datePaiement = datePaiement,
          mode = parts(5)
        ))
      } else None
    }.getOrElse(None)
  }
}