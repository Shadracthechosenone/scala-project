package repository

import config.DatabaseConnection
import universite.model.Matiere
import java.sql.ResultSet
import scala.util.{Try, Using}

class MatiereRepository {

  private def fromRs(rs: ResultSet) = Matiere(
    rs.getString("id_matiere"), rs.getString("nom_matiere"), rs.getString("ue"),
    rs.getInt("coefficient"),   rs.getInt("volume_horaire"),
    Option(rs.getString("enseignant"))
  )

  def creer(m: Matiere): Try[Matiere] =
    Using(DatabaseConnection.getConnection) { conn =>
      val st = conn.prepareStatement(
        "INSERT INTO matieres(id_matiere,nom_matiere,ue,coefficient,volume_horaire,enseignant) VALUES(?,?,?,?,?,?)"
      )
      st.setString(1,m.idMatiere); st.setString(2,m.nomMatiere); st.setString(3,m.ue)
      st.setInt(4,m.coefficient);  st.setInt(5,m.volumeHoraire)
      m.enseignant match {
        case Some(e) => st.setString(6, e)
        case None    => st.setNull(6, java.sql.Types.VARCHAR)
      }
      st.executeUpdate(); m
    }

  def trouverParId(id: String): Option[Matiere] =
    Using(DatabaseConnection.getConnection) { conn =>
      val st = conn.prepareStatement("SELECT * FROM matieres WHERE id_matiere = ?")
      st.setString(1, id)
      val rs = st.executeQuery()
      if (rs.next()) Some(fromRs(rs)) else None
    }.getOrElse(None)

  def toutes(): List[Matiere] =
    Using(DatabaseConnection.getConnection) { conn =>
      val rs = conn.prepareStatement("SELECT * FROM matieres ORDER BY nom_matiere").executeQuery()
      Iterator.continually(rs).takeWhile(_.next()).map(fromRs).toList
    }.getOrElse(List.empty)

  def parEnseignant(idEnseignant: String): List[Matiere] =
    Using(DatabaseConnection.getConnection) { conn =>
      val st = conn.prepareStatement("SELECT * FROM matieres WHERE enseignant = ?")
      st.setString(1, idEnseignant)
      val rs = st.executeQuery()
      Iterator.continually(rs).takeWhile(_.next()).map(fromRs).toList
    }.getOrElse(List.empty)

  def modifier(m: Matiere): Try[Matiere] =
    Using(DatabaseConnection.getConnection) { conn =>
      val st = conn.prepareStatement(
        "UPDATE matieres SET nom_matiere=?,ue=?,coefficient=?,volume_horaire=?,enseignant=? WHERE id_matiere=?"
      )
      st.setString(1,m.nomMatiere); st.setString(2,m.ue)
      st.setInt(3,m.coefficient);   st.setInt(4,m.volumeHoraire)
      m.enseignant match {
        case Some(e) => st.setString(5, e)
        case None    => st.setNull(5, java.sql.Types.VARCHAR)
      }
      st.setString(6, m.idMatiere)
      st.executeUpdate(); m
    }

  def supprimer(id: String): Try[Boolean] =
    Using(DatabaseConnection.getConnection) { conn =>
      val st = conn.prepareStatement("DELETE FROM matieres WHERE id_matiere = ?")
      st.setString(1, id); st.executeUpdate() > 0
    }

  def existe(id: String): Boolean = trouverParId(id).isDefined
}