package repository

import config.DatabaseConnection
import model.Absence

import java.sql.{Date, ResultSet}
import scala.util.{Try, Using}

class AbsenceRepository {

  private def fromResultSet(rs: ResultSet): Absence = Absence(
    idAbsence   = rs.getString("id_absence"),
    matricule   = rs.getString("matricule"),
    matiere     = rs.getString("matiere"),
    dateAbsence = rs.getDate("date_absence").toLocalDate,
    heures      = rs.getInt("heures"),
    justifiee   = rs.getBoolean("justifiee")
  )

  def creer(a: Absence): Try[Absence] = {
    val sql = """
      INSERT INTO absences (id_absence, matricule, matiere, date_absence, heures, justifiee)
      VALUES (?, ?, ?, ?, ?, ?)
    """
    Using(DatabaseConnection.getConnection) { conn =>
      val stmt = conn.prepareStatement(sql)
      stmt.setString(1, a.idAbsence)
      stmt.setString(2, a.matricule)
      stmt.setString(3, a.matiere)
      stmt.setDate(4, Date.valueOf(a.dateAbsence))
      stmt.setInt(5, a.heures)
      stmt.setBoolean(6, a.justifiee)
      stmt.executeUpdate()
      a
    }
  }

  def trouverParId(idAbsence: String): Option[Absence] = {
    val sql = "SELECT * FROM absences WHERE id_absence = ?"
    Using(DatabaseConnection.getConnection) { conn =>
      val stmt = conn.prepareStatement(sql)
      stmt.setString(1, idAbsence)
      val rs = stmt.executeQuery()
      if (rs.next()) Some(fromResultSet(rs)) else None
    }.getOrElse(None)  // Try[Option[Absence]].getOrElse(None) => Option[Absence]
  }

  def trouverParMatricule(matricule: String): List[Absence] = {
    val sql = "SELECT * FROM absences WHERE matricule = ? ORDER BY date_absence DESC"
    Using(DatabaseConnection.getConnection) { conn =>
      val stmt = conn.prepareStatement(sql)
      stmt.setString(1, matricule)
      val rs = stmt.executeQuery()
      Iterator.continually(rs).takeWhile(_.next()).map(fromResultSet).toList
    }.getOrElse(List.empty)  // Try[List[Absence]].getOrElse(List.empty) => List[Absence]
  }

  def trouverNonJustifiees(): List[Absence] = {
    val sql = "SELECT * FROM absences WHERE justifiee = false ORDER BY date_absence DESC"
    Using(DatabaseConnection.getConnection) { conn =>
      val stmt = conn.prepareStatement(sql)
      val rs   = stmt.executeQuery()
      Iterator.continually(rs).takeWhile(_.next()).map(fromResultSet).toList
    }.getOrElse(List.empty)
  }

  def totalHeuresParMatricule(matricule: String): Int = {
    val sql = "SELECT COALESCE(SUM(heures), 0) AS total FROM absences WHERE matricule = ?"
    Using(DatabaseConnection.getConnection) { conn =>
      val stmt = conn.prepareStatement(sql)
      stmt.setString(1, matricule)
      val rs = stmt.executeQuery()
      if (rs.next()) rs.getInt("total") else 0
    }.getOrElse(0)  // Try[Int].getOrElse(0) => Int
  }

  def toutesLesAbsences(): List[Absence] = {
    val sql = "SELECT * FROM absences ORDER BY date_absence DESC"
    Using(DatabaseConnection.getConnection) { conn =>
      val stmt = conn.prepareStatement(sql)
      val rs   = stmt.executeQuery()
      Iterator.continually(rs).takeWhile(_.next()).map(fromResultSet).toList
    }.getOrElse(List.empty)
  }

  def modifier(a: Absence): Try[Absence] = {
    val sql = """
      UPDATE absences
      SET matricule = ?, matiere = ?, date_absence = ?, heures = ?, justifiee = ?
      WHERE id_absence = ?
    """
    Using(DatabaseConnection.getConnection) { conn =>
      val stmt = conn.prepareStatement(sql)
      stmt.setString(1, a.matricule)
      stmt.setString(2, a.matiere)
      stmt.setDate(3, Date.valueOf(a.dateAbsence))
      stmt.setInt(4, a.heures)
      stmt.setBoolean(5, a.justifiee)
      stmt.setString(6, a.idAbsence)
      stmt.executeUpdate()
      a
    }
  }

  def supprimer(idAbsence: String): Try[Boolean] = {
    val sql = "DELETE FROM absences WHERE id_absence = ?"
    Using(DatabaseConnection.getConnection) { conn =>
      val stmt = conn.prepareStatement(sql)
      stmt.setString(1, idAbsence)
      stmt.executeUpdate() > 0
    }
  }

  def existe(idAbsence: String): Boolean =
    trouverParId(idAbsence).isDefined
}