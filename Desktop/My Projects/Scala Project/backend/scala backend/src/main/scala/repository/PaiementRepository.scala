package repository

import config.DatabaseConnection
import model.Paiement

import java.sql.{Date => SqlDate, ResultSet}
import scala.util.{Try, Using}

class PaiementRepository {

  private def fromResultSet(rs: ResultSet): Paiement = Paiement(
    idPaiement   = rs.getString("id_paiement"),
    matricule    = rs.getString("matricule"),
    montantTotal = rs.getDouble("montant_total"),
    montantPaye  = rs.getDouble("montant_paye"),
    datePaiement = Option(rs.getDate("date_paiement")).map(_.toLocalDate),
    mode         = rs.getString("mode")
  )

  def creer(p: Paiement): Try[Paiement] =
    Using(DatabaseConnection.getConnection) { conn =>
      val sql =
        """
          INSERT INTO paiements (id_paiement, matricule, montant_total, montant_paye, date_paiement, mode)
          VALUES (?, ?, ?, ?, ?, ?)
        """
      val stmt = conn.prepareStatement(sql)
      stmt.setString(1, p.idPaiement)
      stmt.setString(2, p.matricule)
      stmt.setDouble(3, p.montantTotal)
      stmt.setDouble(4, p.montantPaye)
      p.datePaiement match {
        case Some(d) => stmt.setDate(5, SqlDate.valueOf(d))
        case None    => stmt.setNull(5, java.sql.Types.DATE)
      }
      stmt.setString(6, p.mode)
      stmt.executeUpdate()
      p
    }

  def trouverParId(idPaiement: String): Option[Paiement] =
    Using(DatabaseConnection.getConnection) { conn =>
      val stmt = conn.prepareStatement("SELECT * FROM paiements WHERE id_paiement = ?")
      stmt.setString(1, idPaiement)
      val rs = stmt.executeQuery()
      if (rs.next()) Some(fromResultSet(rs)) else None
    }.getOrElse(None)

  def trouverParMatricule(matricule: String): List[Paiement] =
    Using(DatabaseConnection.getConnection) { conn =>
      val stmt = conn.prepareStatement(
        "SELECT * FROM paiements WHERE matricule = ? ORDER BY date_paiement DESC"
      )
      stmt.setString(1, matricule)
      val rs = stmt.executeQuery()
      Iterator.continually(rs).takeWhile(_.next()).map(fromResultSet).toList
    }.getOrElse(List.empty)

  def tousLesPaiements(): List[Paiement] =
    Using(DatabaseConnection.getConnection) { conn =>
      val stmt = conn.prepareStatement("SELECT * FROM paiements ORDER BY date_paiement DESC")
      val rs   = stmt.executeQuery()
      Iterator.continually(rs).takeWhile(_.next()).map(fromResultSet).toList
    }.getOrElse(List.empty)

  def modifier(p: Paiement): Try[Paiement] =
    Using(DatabaseConnection.getConnection) { conn =>
      val sql =
        """
          UPDATE paiements
          SET matricule = ?, montant_total = ?, montant_paye = ?, date_paiement = ?, mode = ?
          WHERE id_paiement = ?
        """
      val stmt = conn.prepareStatement(sql)
      stmt.setString(1, p.matricule)
      stmt.setDouble(2, p.montantTotal)
      stmt.setDouble(3, p.montantPaye)
      p.datePaiement match {
        case Some(d) => stmt.setDate(4, SqlDate.valueOf(d))
        case None    => stmt.setNull(4, java.sql.Types.DATE)
      }
      stmt.setString(5, p.mode)
      stmt.setString(6, p.idPaiement)
      stmt.executeUpdate()
      p
    }

  def supprimer(idPaiement: String): Try[Boolean] =
    Using(DatabaseConnection.getConnection) { conn =>
      val stmt = conn.prepareStatement("DELETE FROM paiements WHERE id_paiement = ?")
      stmt.setString(1, idPaiement)
      stmt.executeUpdate() > 0
    }

  def totalEncaisseParMatricule(matricule: String): Double =
    Using(DatabaseConnection.getConnection) { conn =>
      val stmt = conn.prepareStatement(
        "SELECT COALESCE(SUM(montant_paye), 0) AS total FROM paiements WHERE matricule = ?"
      )
      stmt.setString(1, matricule)
      val rs = stmt.executeQuery()
      if (rs.next()) rs.getDouble("total") else 0.0
    }.getOrElse(0.0)

  def totalEncaisseGlobal(): Double =
    Using(DatabaseConnection.getConnection) { conn =>
      val stmt = conn.prepareStatement(
        "SELECT COALESCE(SUM(montant_paye), 0) AS total FROM paiements"
      )
      val rs = stmt.executeQuery()
      if (rs.next()) rs.getDouble("total") else 0.0
    }.getOrElse(0.0)

  def totalAttenduGlobal(): Double =
    Using(DatabaseConnection.getConnection) { conn =>
      val stmt = conn.prepareStatement(
        "SELECT COALESCE(SUM(montant_total), 0) AS total FROM paiements"
      )
      val rs = stmt.executeQuery()
      if (rs.next()) rs.getDouble("total") else 0.0
    }.getOrElse(0.0)

  def matriculesEnDette(): List[String] =
    Using(DatabaseConnection.getConnection) { conn =>
      val stmt = conn.prepareStatement(
        """
          SELECT DISTINCT matricule
          FROM paiements
          WHERE montant_paye < montant_total
          ORDER BY matricule
        """
      )
      val rs = stmt.executeQuery()
      Iterator.continually(rs).takeWhile(_.next()).map(_.getString("matricule")).toList
    }.getOrElse(List.empty)

  def existe(idPaiement: String): Boolean =
    trouverParId(idPaiement).isDefined
}