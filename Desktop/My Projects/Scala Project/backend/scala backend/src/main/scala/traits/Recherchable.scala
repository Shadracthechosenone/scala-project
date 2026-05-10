package traits

trait Recherchable[T] {
  def rechercherParId(id: String): Option[T]
  def rechercherParCritere(critere: String => Boolean): List[T]
  def existe(id: String): Boolean
}

