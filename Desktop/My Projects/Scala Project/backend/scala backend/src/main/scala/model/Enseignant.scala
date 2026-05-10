package model

case class Enseignant(
                       idEnseignant: String,
                       nom:          String,
                       prenom:       String,
                       grade:        String,
                       specialite:   String,
                       departement:  String,
                       email:        String,
                       telephone:    String
                     )