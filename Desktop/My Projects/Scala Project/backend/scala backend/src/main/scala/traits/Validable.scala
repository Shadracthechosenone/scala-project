package traits

trait Validable {
  def isValid: Boolean
  // validate() optionnel avec implémentation par défaut
  def validate(): List[String] = List.empty
}