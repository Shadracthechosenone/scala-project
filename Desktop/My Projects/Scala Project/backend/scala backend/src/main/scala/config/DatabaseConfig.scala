package config

import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.PostgresProfile.api._

import java.sql.Connection

object DatabaseConnection extends LazyLogging {

  private val conf = ConfigFactory.load()

  // ── HikariCP config ────────────────────────────────────────────────────────
  private val hikariConfig: HikariConfig = {
    val hc = new HikariConfig()
    hc.setJdbcUrl(conf.getString("db.url"))
    hc.setUsername(conf.getString("db.user"))
    hc.setPassword(conf.getString("db.password"))
    hc.setDriverClassName(conf.getString("db.driver"))
    hc.setMaximumPoolSize(conf.getInt("db.pool.maximumPoolSize"))
    hc.setMinimumIdle(conf.getInt("db.pool.minimumIdle"))
    hc.setConnectionTimeout(conf.getLong("db.pool.connectionTimeout"))
    hc.setIdleTimeout(conf.getLong("db.pool.idleTimeout"))
    hc.setPoolName("UniversitePool")
    hc
  }

  // ── DataSource JDBC brut (pour requêtes manuelles si besoin) ───────────────
  private val dataSource: HikariDataSource = {
    logger.info("Initialisation du pool de connexions PostgreSQL...")
    new HikariDataSource(hikariConfig)
  }

  // ── Instance Slick — réutilise le même DataSource HikariCP ─────────────────
  // Slick gère lui-même le threading via son ExecutionContext interne.
  val db: Database = {
    logger.info("Initialisation de la Database Slick sur le pool HikariCP...")
    Database.forDataSource(
      dataSource,
      maxConnections = Some(conf.getInt("db.pool.maximumPoolSize"))
    )
  }

  // ── API JDBC brute (repositories non-Slick) ────────────────────────────────
  def getConnection(): Connection = dataSource.getConnection()

  def withConnection[A](block: Connection => A): A = {
    val conn = getConnection()
    try {
      block(conn)
    } finally {
      conn.close()
    }
  }

  // ── Fermeture propre (Main / shutdown hook) ────────────────────────────────
  def shutdown(): Unit = {
    logger.info("Fermeture de la Database Slick et du pool HikariCP...")
    db.close()           // ferme Slick en premier
    dataSource.close()   // puis le pool sous-jacent
  }
}