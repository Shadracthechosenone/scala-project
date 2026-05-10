package traits

trait Affichable {
  def afficher(): String
  def afficherDetails(): Unit = println(afficher())
}