CREATE DATABASE university_management;

\c universite_db;

-- Table des étudiants
CREATE TABLE etudiants (
    matricule VARCHAR(20) PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    sexe VARCHAR(1) CHECK (sexe IN ('M', 'F')),
    date_naissance DATE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    telephone VARCHAR(20),
    filiere VARCHAR(50) NOT NULL,
    niveau VARCHAR(10) NOT NULL,
    annee_academique VARCHAR(20) NOT NULL,
    statut VARCHAR(20) CHECK (statut IN ('Actif', 'Suspendu', 'Diplome'))
);

-- Table des enseignants
CREATE TABLE enseignants (
    id_enseignant VARCHAR(20) PRIMARY KEY,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    grade VARCHAR(50) NOT NULL,
    specialite VARCHAR(100) NOT NULL,
    departement VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    telephone VARCHAR(20)
);

-- Table des filieres
CREATE TABLE filieres (
    id_filiere VARCHAR(20) PRIMARY KEY,
    nom_filiere VARCHAR(50) UNIQUE NOT NULL,
    responsable VARCHAR(20) REFERENCES enseignants(id_enseignant)
);

-- Table des matieres
CREATE TABLE matieres (
    id_matiere VARCHAR(20) PRIMARY KEY,
    nom_matiere VARCHAR(100) NOT NULL,
    ue VARCHAR(100) NOT NULL,
    coefficient INTEGER NOT NULL,
    volume_horaire INTEGER NOT NULL,
    enseignant VARCHAR(20) REFERENCES enseignants(id_enseignant)
);

-- Table des inscriptions
CREATE TABLE inscriptions (
    id_inscription VARCHAR(20) PRIMARY KEY,
    matricule VARCHAR(20) REFERENCES etudiants(matricule),
    filiere VARCHAR(50) NOT NULL,
    niveau VARCHAR(10) NOT NULL,
    annee VARCHAR(20) NOT NULL,
    statut VARCHAR(20) CHECK (statut IN ('Validee', 'En attente', 'Annulee')),
    UNIQUE(matricule, annee)
);

-- Table des notes
CREATE TABLE notes (
    id_note VARCHAR(20) PRIMARY KEY,
    matricule VARCHAR(20) REFERENCES etudiants(matricule),
    matiere VARCHAR(20) REFERENCES matieres(id_matiere),
    controle_continu DECIMAL(5,2),
    examen DECIMAL(5,2),
    CHECK (controle_continu >= 0 AND controle_continu <= 20),
    CHECK (examen >= 0 AND examen <= 20)
);

-- Table des absences
CREATE TABLE absences (
    id_absence VARCHAR(20) PRIMARY KEY,
    matricule VARCHAR(20) REFERENCES etudiants(matricule),
    matiere VARCHAR(20) REFERENCES matieres(id_matiere),
    date_absence DATE NOT NULL,
    heures INTEGER NOT NULL,
    justifiee BOOLEAN DEFAULT FALSE
);

-- Table des paiements
CREATE TABLE paiements (
    id_paiement VARCHAR(20) PRIMARY KEY,
    matricule VARCHAR(20) REFERENCES etudiants(matricule),
    montant_total DECIMAL(15,2) NOT NULL,
    montant_paye DECIMAL(15,2) DEFAULT 0,
    date_paiement DATE,
    mode VARCHAR(50)
);

-- Table des salles
CREATE TABLE salles (
    id_salle VARCHAR(20) PRIMARY KEY,
    nom_salle VARCHAR(50) NOT NULL,
    capacite INTEGER NOT NULL,
    type VARCHAR(50) NOT NULL
);

-- Table des emplois du temps

CREATE TABLE emplois_du_temps (
    id_seance VARCHAR(20) PRIMARY KEY,
    matiere VARCHAR(20) REFERENCES matieres(id_matiere),
    enseignant VARCHAR(20) REFERENCES enseignants(id_enseignant),
    salle VARCHAR(20) REFERENCES salles(id_salle),
    jour VARCHAR(10) NOT NULL,
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    filiere VARCHAR(50) NOT NULL,
    niveau VARCHAR(10) NOT NULL,
    CHECK (heure_debut < heure_fin)
);
