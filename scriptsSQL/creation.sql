CREATE TABLE Ligue(
	nomLigue	VARCHAR(255) NOT NULL,
	CONSTRAINT primary_key_ligue PRIMARY KEY(nomLigue) 
);

CREATE TABLE Equipe(
	nomEquipe	VARCHAR(255) NOT NULL,
	NbreJoueurMax INTEGER NOT NULL,
	nomLigue	VARCHAR(255) NOT NULL REFERENCES Ligue,
	CONSTRAINT primary_key_equipe PRIMARY KEY(nomEquipe)
);

CREATE TABLE Participant(
	matricule	CHAR(8) NOT NULL,
	age INTEGER NOT NULL,
	nom	VARCHAR(255) NOT NULL ,
	prenom	VARCHAR(255) NOT NULL ,
	CONSTRAINT primary_key_participant PRIMARY KEY(matricule)
);



CREATE TABLE Joueur(
	nomEquipe	VARCHAR(255) NOT NULL REFERENCES Equipe,
	matricule CHAR(8) NOT NULL REFERENCES Participant
);


CREATE TABLE Resultat(
	Id	INTEGER NOT NULL,
	scoreEquipeA INTEGER NOT NULL,
	scoreEquipeB INTEGER NOT NULL,
	nomEquipeA	VARCHAR(255) NOT NULL REFERENCES Equipe(nomEquipe) ON DELETE CASCADE,
	nomEquipeB	VARCHAR(255) NOT NULL REFERENCES Equipe(nomEquipe) ON DELETE CASCADE,
	CONSTRAINT primary_key_resultat PRIMARY KEY(Id)
);