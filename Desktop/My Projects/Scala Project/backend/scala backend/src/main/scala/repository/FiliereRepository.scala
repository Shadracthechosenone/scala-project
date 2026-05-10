package universite.repository

import universite.model.Filiere
import scala.collection.mutable

class FiliereRepository {
  private val data = mutable.ListBuffer(
    Filiere("FIL001", "Informatique", "ENS001"),
    Filiere("FIL002", "Data Science", "ENS003")
  )

  def findAll(): List[Filiere] = data.toList

  def findById(id: String): Option[Filiere] = data.find(_.idFiliere == id)

  def insert(f: Filiere): Either[String, Filiere] =
    if (data.exists(_.idFiliere == f.idFiliere)) Left("ID déjà existant")
    else { data += f; Right(f) }

  def update(f: Filiere): Either[String, Filiere] =
    data.indexWhere(_.idFiliere == f.idFiliere) match {
      case -1 => Left("Filière introuvable")
      case i  => data(i) = f; Right(f)
    }

  def delete(id: String): Boolean =
    data.find(_.idFiliere == id).map { f => data -= f; true }.getOrElse(false)
}