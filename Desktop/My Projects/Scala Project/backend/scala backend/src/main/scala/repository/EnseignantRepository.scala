package repository

import config.DatabaseConnection
import model.Enseignant
import java.sql.ResultSet
import scala.util.{Try, Using}

class EnseignantRepository {

  private def fromRs(rs: ResultSet) = Enseignant(
    rs.getString("id_enseignant"), rs.getString("nom"),      rs.getString("prenom"),
    rs.getString("grade"),         rs.getString("specialite"), rs.getString("departement"),
    rs.getString("email"),         rs.getString("telephone")
  )

  def creer(e: Enseignant): Try[Enseignant] =
    Using(DatabaseConnection.getConnection) { conn =>
      val st = conn.prepareStatement(
        "INSERT INTO enseignants(id_enseignant,nom,prenom,grade,specialite,departement,email,telephone) VALUES(?,?,?,?,?,?,?,?)"
      )
      st.setString(1,e.idEnseignant); st.setString(2,e.nom);        st.setString(3,e.prenom)
      st.setString(4,e.grade);        st.setString(5,e.specialite); st.setString(6,e.departement)
      st.setString(7,e.email);        st.setString(8,e.telephone)
      st.executeUpdate(); e
    }

  def trouverParId(id: String): Option[Enseignant] =
    Using(DatabaseConnection.getConnection) { conn =>
      val st = conn.prepareStatement("SELECT * FROM enseignants WHERE id_enseignant = ?")
      st.setString(1, id)
      val rs = st.executeQuery()
      if (rs.next()) Some(fromRs(rs)) else None
    }.getOrElse(None)

  def tous(): List[Enseignant] =
    Using(DatabaseConnection.getConnection) { conn =>
      val rs = conn.prepareStatement("SELECT * FROM enseignants ORDER BY nom").executeQuery()
      Iterator.continually(rs).takeWhile(_.next()).map(fromRs).toList
    }.getOrElse(List.empty)

  def modifier(e: Enseignant): Try[Enseignant] =
    Using(DatabaseConnection.getConnection) { conn =>
      val st = conn.prepareStatement(
        "UPDATE enseignants SET nom=?,prenom=?,grade=?,specialite=?,departement=?,email=?,telephone=? WHERE id_enseignant=?"
      )
      st.setString(1,e.nom);   st.setString(2,e.prenom);      st.setString(3,e.grade)
      st.setString(4,e.specialite); st.setString(5,e.departement); st.setString(6,e.email)
      st.setString(7,e.telephone);  st.setString(8,e.idEnseignant)
      st.executeUpdate(); e
    }

  def supprimer(id: String): Try[Boolean] =
    Using(DatabaseConnection.getConnection) { conn =>
      val st = conn.prepareStatement("DELETE FROM enseignants WHERE id_enseignant = ?")
      st.setString(1, id); st.executeUpdate() > 0
    }

  def existe(id: String): Boolean = trouverParId(id).isDefined
}