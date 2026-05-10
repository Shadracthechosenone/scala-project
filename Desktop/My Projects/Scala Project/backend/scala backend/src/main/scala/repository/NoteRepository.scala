package repository

import config.DatabaseConnection
import universite.model.{Matiere, Note}
import com.typesafe.scalalogging.LazyLogging

import java.sql.ResultSet
import scala.collection.mutable.ListBuffer
import scala.util.Try

class NoteRepository extends LazyLogging {

  // ── Mappers ───────────────────────────────────────────────────────────

  private def mapNote(rs: ResultSet): Note = Note(
    idNote          = rs.getString("id_note"),
    matricule       = rs.getString("matricule"),
    matiere         = rs.getString("matiere"),
    controleContinu = Option(rs.getBigDecimal("controle_continu"))
      .map(_.doubleValue).getOrElse(0.0),
    examen          = Option(rs.getBigDecimal("examen"))
      .map(_.doubleValue).getOrElse(0.0)
  )

  private def mapMatiere(rs: ResultSet): Matiere = Matiere(
    idMatiere     = rs.getString("id_matiere"),
    nomMatiere    = rs.getString("nom_matiere"),
    ue            = rs.getString("ue"),
    coefficient   = rs.getInt("coefficient"),
    volumeHoraire = rs.getInt("volume_horaire"),
    enseignant    = Option(rs.getString("enseignant"))
  )

  // ── Notes CRUD ────────────────────────────────────────────────────────

  def creer(note: Note): Try[Note] = Try {
    val sql =
      """INSERT INTO notes (id_note, matricule, matiere, controle_continu, examen)
        |VALUES (?, ?, ?, ?, ?)""".stripMargin
    DatabaseConnection.withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      ps.setString(1, note.idNote)
      ps.setString(2, note.matricule)
      ps.setString(3, note.matiere)
      ps.setDouble(4, note.controleContinu)
      ps.setDouble(5, note.examen)
      ps.executeUpdate()
      ps.close()
      logger.info(s"Note ${note.idNote} créée.")
      note
    }
  }

  def modifier(note: Note): Try[Note] = Try {
    val sql =
      """UPDATE notes SET controle_continu = ?, examen = ?
        |WHERE id_note = ?""".stripMargin
    DatabaseConnection.withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      ps.setDouble(1, note.controleContinu)
      ps.setDouble(2, note.examen)
      ps.setString(3, note.idNote)
      val n = ps.executeUpdate()
      ps.close()
      if (n == 0) throw new NoSuchElementException(s"Note ${note.idNote} introuvable.")
      note
    }
  }

  def supprimer(idNote: String): Try[Boolean] = Try {
    val sql = "DELETE FROM notes WHERE id_note = ?"
    DatabaseConnection.withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      ps.setString(1, idNote)
      val n = ps.executeUpdate()
      ps.close()
      n > 0
    }
  }

  def trouverParId(idNote: String): Option[Note] = {
    val sql = "SELECT * FROM notes WHERE id_note = ?"
    DatabaseConnection.withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      ps.setString(1, idNote)
      val rs = ps.executeQuery()
      val r  = if (rs.next()) Some(mapNote(rs)) else None
      rs.close(); ps.close(); r
    }
  }

  def trouverParMatricule(matricule: String): List[Note] = {
    val sql    = "SELECT * FROM notes WHERE matricule = ?"
    val buffer = ListBuffer[Note]()
    DatabaseConnection.withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      ps.setString(1, matricule)
      val rs = ps.executeQuery()
      while (rs.next()) buffer += mapNote(rs)
      rs.close(); ps.close()
    }
    buffer.toList
  }

  def trouverParMatiere(idMatiere: String): List[Note] = {
    val sql    = "SELECT * FROM notes WHERE matiere = ?"
    val buffer = ListBuffer[Note]()
    DatabaseConnection.withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      ps.setString(1, idMatiere)
      val rs = ps.executeQuery()
      while (rs.next()) buffer += mapNote(rs)
      rs.close(); ps.close()
    }
    buffer.toList
  }

  def toutesLesNotes(): List[Note] = {
    val sql    = "SELECT * FROM notes"
    val buffer = ListBuffer[Note]()
    DatabaseConnection.withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      val rs = ps.executeQuery()
      while (rs.next()) buffer += mapNote(rs)
      rs.close(); ps.close()
    }
    buffer.toList
  }

  def existe(idNote: String): Boolean = {
    val sql = "SELECT 1 FROM notes WHERE id_note = ?"
    DatabaseConnection.withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      ps.setString(1, idNote)
      val rs = ps.executeQuery()
      val r  = rs.next()
      rs.close(); ps.close(); r
    }
  }

  // ── Matieres ──────────────────────────────────────────────────────────

  def toutesLesMatieres(): List[Matiere] = {
    val sql    = "SELECT * FROM matieres ORDER BY ue, nom_matiere"
    val buffer = ListBuffer[Matiere]()
    DatabaseConnection.withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      val rs = ps.executeQuery()
      while (rs.next()) buffer += mapMatiere(rs)
      rs.close(); ps.close()
    }
    buffer.toList
  }

  def trouverMatiereParId(idMatiere: String): Option[Matiere] = {
    val sql = "SELECT * FROM matieres WHERE id_matiere = ?"
    DatabaseConnection.withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      ps.setString(1, idMatiere)
      val rs = ps.executeQuery()
      val r  = if (rs.next()) Some(mapMatiere(rs)) else None
      rs.close(); ps.close(); r
    }
  }
}