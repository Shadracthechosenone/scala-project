package universite.model

case class Seance(
                   idSeance:   String,
                   matiere:    String,
                   enseignant: String,
                   salle:      String,
                   jour:       String,
                   heureDebut: String, // "HH:mm"
                   heureFin:   String, // "HH:mm"
                   filiere:    String,
                   niveau:     String
                 )