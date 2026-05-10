package repository

import universite.model.Salle

class SalleRepository {

  private val typesValides = Set("Amphitheatre", "TD", "TP", "Informatique", "Conference")

  def findAll(): List[Salle]                = List.empty
  def findById(id: String): Option[Salle]  = None
  def findByType(t: String): List[Salle]   = List.empty

  def insert(s: Salle): Either[String, Salle] =
    if (s.nomSalle.isBlank)
      Left("Le nom de la salle est obligatoire")
    else if (s.capacite <= 0)
      Left("La capacité doit être un entier positif")
    else if (!typesValides.contains(s.typeSalle))
      Left(s"Type invalide : '${s.typeSalle}'. Valeurs acceptées : ${typesValides.mkString(", ")}")
    else
      Right(s)

  def update(s: Salle): Either[String, Salle] =
    if (s.nomSalle.isBlank)
      Left("Le nom de la salle est obligatoire")
    else if (s.capacite <= 0)
      Left("La capacité doit être un entier positif")
    else if (!typesValides.contains(s.typeSalle))
      Left(s"Type invalide : '${s.typeSalle}'")
    else
      Right(s)

  def delete(id: String): Boolean = true
}