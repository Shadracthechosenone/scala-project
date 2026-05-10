package repository

import universite.model.Inscription

class InscriptionRepository {

  private val statutsValides = Set("Validee", "EnAttente", "Annulee")

  def findAll(): List[Inscription]                  = List.empty
  def findById(id: String): Option[Inscription]     = None
  def findByMatricule(m: String): List[Inscription] = List.empty
  def findByFiliere(f: String): List[Inscription]   = List.empty

  def insert(i: Inscription): Either[String, Inscription] =
    if (!statutsValides.contains(i.statut))
      Left(s"Statut invalide : '${i.statut}'. Valeurs acceptées : ${statutsValides.mkString(", ")}")
    else
      Right(i)

  def update(i: Inscription): Either[String, Inscription] =
    if (!statutsValides.contains(i.statut))
      Left(s"Statut invalide : '${i.statut}'. Valeurs acceptées : ${statutsValides.mkString(", ")}")
    else
      Right(i)

  def delete(id: String): Boolean = true
}