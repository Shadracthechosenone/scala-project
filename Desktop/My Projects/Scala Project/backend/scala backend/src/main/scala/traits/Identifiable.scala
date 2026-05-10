package traits

trait Identifiable {
  def id: String
  def getIdentifier: String = id
}