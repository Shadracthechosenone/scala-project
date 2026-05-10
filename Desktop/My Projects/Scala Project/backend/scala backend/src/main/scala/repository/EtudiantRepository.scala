package repository

import config.DatabaseConnection
import universite.model.{Etudiant, StatutEtudiant}
import com.typesafe.scalalogging.LazyLogging

import java.sql.{Date, PreparedStatement, ResultSet}
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

/**
 * Repository du module Etudiant.
 * Toutes les operations base de donnees (PostgreSQL) sont ici.
 * Le service ne connait pas SQL — il passe uniquement par ce repository.
 */
class EtudiantRepository extends LazyLogging {

  // ─────────────────────────────────────────────────
  // METHODE PRIVEE : convertit un ResultSet -> Etudiant
  // ─────────────────────────────────────────────────
  private def mapperResultSet(rs: ResultSet): Etudiant = {
    val telOption = Option(rs.getString("telephone")).filter(_.nonEmpty)
    Etudiant(
      matricule       = rs.getString("matricule"),
      nom             = rs.getString("nom"),
      prenom          = rs.getString("prenom"),
      sexe            = rs.getString("sexe").charAt(0),
      dateNaissance   = rs.getDate("date_naissance").toLocalDate,
      email           = rs.getString("email"),
      telephone       = telOption,
      filiere         = rs.getString("filiere"),
      niveau          = rs.getString("niveau"),
      anneeAcademique = rs.getString("annee_academique"),
      statut          = StatutEtudiant.fromString(rs.getString("statut"))
    )
  }

  // ─────────────────────────────────────────────────
  // CREATE
  // ─────────────────────────────────────────────────

  /**
   * Insere un nouvel etudiant en base.
   * Retourne Success(etudiant) ou Failure(exception).
   */
  def creer(etudiant: Etudiant): Try[Etudiant] = Try {
    val sql =
      """INSERT INTO etudiants
        |  (matricule, nom, prenom, sexe, date_naissance, email,
        |   telephone, filiere, niveau, annee_academique, statut)
        |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      """.stripMargin

    DatabaseConnection.withConnection { conn =>
      val ps: PreparedStatement = conn.prepareStatement(sql)
      ps.setString(1, etudiant.matricule)
      ps.setString(2, etudiant.nom)
      ps.setString(3, etudiant.prenom)
      ps.setString(4, etudiant.sexe.toString)
      ps.setDate(5, Date.valueOf(etudiant.dateNaissance))
      ps.setString(6, etudiant.email)
      ps.setString(7, etudiant.telephone.orNull)
      ps.setString(8, etudiant.filiere)
      ps.setString(9, etudiant.niveau)
      ps.setString(10, etudiant.anneeAcademique)
      ps.setString(11, StatutEtudiant.toString(etudiant.statut))
      ps.executeUpdate()
      ps.close()
      logger.info(s"Etudiant ${etudiant.matricule} cree avec succes.")
      etudiant
    }
  }

  // ─────────────────────────────────────────────────
  // READ — trouver par matricule
  // ─────────────────────────────────────────────────

  /**
   * Recherche un etudiant par son matricule.
   * Retourne Some(etudiant) s'il existe, None sinon.
   */
  def trouverParMatricule(matricule: String): Option[Etudiant] = {
    val sql = "SELECT * FROM etudiants WHERE matricule = ?"
    DatabaseConnection.withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      ps.setString(1, matricule)
      val rs = ps.executeQuery()
      val result = if (rs.next()) Some(mapperResultSet(rs)) else None
      rs.close()
      ps.close()
      result
    }
  }

  // ─────────────────────────────────────────────────
  // READ — tous les etudiants
  // ─────────────────────────────────────────────────

  /** Retourne la liste complete de tous les etudiants. */
  def trouverTous(): List[Etudiant] = {
    val sql    = "SELECT * FROM etudiants ORDER BY nom, prenom"
    val buffer = ListBuffer[Etudiant]()

    DatabaseConnection.withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      val rs = ps.executeQuery()
      while (rs.next()) buffer += mapperResultSet(rs)
      rs.close()
      ps.close()
    }
    buffer.toList
  }

  // ─────────────────────────────────────────────────
  // READ — filtres
  // ─────────────────────────────────────────────────

  /** Filtre les etudiants par filiere. */
  def trouverParFiliere(filiere: String): List[Etudiant] = {
    val sql    = "SELECT * FROM etudiants WHERE filiere = ? ORDER BY nom"
    val buffer = ListBuffer[Etudiant]()

    DatabaseConnection.withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      ps.setString(1, filiere)
      val rs = ps.executeQuery()
      while (rs.next()) buffer += mapperResultSet(rs)
      rs.close()
      ps.close()
    }
    buffer.toList
  }

  /** Filtre les etudiants par niveau (M1, M2...). */
  def trouverParNiveau(niveau: String): List[Etudiant] = {
    val sql    = "SELECT * FROM etudiants WHERE niveau = ? ORDER BY nom"
    val buffer = ListBuffer[Etudiant]()

    DatabaseConnection.withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      ps.setString(1, niveau)
      val rs = ps.executeQuery()
      while (rs.next()) buffer += mapperResultSet(rs)
      rs.close()
      ps.close()
    }
    buffer.toList
  }

  /** Filtre les etudiants par statut. */
  def trouverParStatut(statut: String): List[Etudiant] = {
    val sql    = "SELECT * FROM etudiants WHERE statut = ? ORDER BY nom"
    val buffer = ListBuffer[Etudiant]()

    DatabaseConnection.withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      ps.setString(1, statut)
      val rs = ps.executeQuery()
      while (rs.next()) buffer += mapperResultSet(rs)
      rs.close()
      ps.close()
    }
    buffer.toList
  }

  // ─────────────────────────────────────────────────
  // UPDATE
  // ─────────────────────────────────────────────────

  /**
   * Met a jour les informations d'un etudiant existant.
   * Retourne Success(etudiant) ou Failure(exception).
   */
  def modifier(etudiant: Etudiant): Try[Etudiant] = Try {
    val sql =
      """UPDATE etudiants SET
        |  nom = ?, prenom = ?, sexe = ?, date_naissance = ?,
        |  email = ?, telephone = ?, filiere = ?, niveau = ?,
        |  annee_academique = ?, statut = ?
        |WHERE matricule = ?
      """.stripMargin

    DatabaseConnection.withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      ps.setString(1, etudiant.nom)
      ps.setString(2, etudiant.prenom)
      ps.setString(3, etudiant.sexe.toString)
      ps.setDate(4, Date.valueOf(etudiant.dateNaissance))
      ps.setString(5, etudiant.email)
      ps.setString(6, etudiant.telephone.orNull)
      ps.setString(7, etudiant.filiere)
      ps.setString(8, etudiant.niveau)
      ps.setString(9, etudiant.anneeAcademique)
      ps.setString(10, StatutEtudiant.toString(etudiant.statut))
      ps.setString(11, etudiant.matricule)

      val lignesAffectees = ps.executeUpdate()
      ps.close()

      if (lignesAffectees == 0)
        throw new NoSuchElementException(s"Etudiant ${etudiant.matricule} introuvable.")

      logger.info(s"Etudiant ${etudiant.matricule} mis a jour.")
      etudiant
    }
  }

  // ─────────────────────────────────────────────────
  // DELETE
  // ─────────────────────────────────────────────────

  /**
   * Supprime un etudiant par son matricule.
   * Retourne true si la suppression a eu lieu, false sinon.
   */
  def supprimer(matricule: String): Try[Boolean] = Try {
    val sql = "DELETE FROM etudiants WHERE matricule = ?"
    DatabaseConnection.withConnection { conn =>
      val ps              = conn.prepareStatement(sql)
      ps.setString(1, matricule)
      val lignesAffectees = ps.executeUpdate()
      ps.close()
      logger.info(s"Suppression etudiant $matricule : $lignesAffectees ligne(s) affectee(s).")
      lignesAffectees > 0
    }
  }

  // ─────────────────────────────────────────────────
  // EXISTENCE
  // ─────────────────────────────────────────────────

  /** Verifie si un matricule existe deja en base. */
  def existe(matricule: String): Boolean = {
    val sql = "SELECT 1 FROM etudiants WHERE matricule = ?"
    DatabaseConnection.withConnection { conn =>
      val ps     = conn.prepareStatement(sql)
      ps.setString(1, matricule)
      val rs     = ps.executeQuery()
      val existe = rs.next()
      rs.close()
      ps.close()
      existe
    }
  }

  /** Verifie si un email est deja utilise. */
  def emailExiste(email: String, matriculeExclu: Option[String] = None): Boolean = {
    val sql = matriculeExclu match {
      case Some(_) => "SELECT 1 FROM etudiants WHERE email = ? AND matricule <> ?"
      case None    => "SELECT 1 FROM etudiants WHERE email = ?"
    }
    DatabaseConnection.withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      ps.setString(1, email)
      matriculeExclu.foreach(m => ps.setString(2, m))
      val rs     = ps.executeQuery()
      val trouve = rs.next()
      rs.close()
      ps.close()
      trouve
    }
  }

  // ─────────────────────────────────────────────────
  // COMPTAGE
  // ─────────────────────────────────────────────────

  /** Compte le nombre total d'etudiants. */
  def compterTous(): Int = {
    val sql = "SELECT COUNT(*) FROM etudiants"
    DatabaseConnection.withConnection { conn =>
      val ps  = conn.prepareStatement(sql)
      val rs  = ps.executeQuery()
      val cnt = if (rs.next()) rs.getInt(1) else 0
      rs.close(); ps.close()
      cnt
    }
  }

  /** Compte les etudiants par statut donne. */
  def compterParStatut(statut: String): Int = {
    val sql = "SELECT COUNT(*) FROM etudiants WHERE statut = ?"
    DatabaseConnection.withConnection { conn =>
      val ps = conn.prepareStatement(sql)
      ps.setString(1, statut)
      val rs  = ps.executeQuery()
      val cnt = if (rs.next()) rs.getInt(1) else 0
      rs.close(); ps.close()
      cnt
    }
  }
}