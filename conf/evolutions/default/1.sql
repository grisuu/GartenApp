# Mitglieder schema
# ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=1

# --- !Ups

CREATE TABLE mitglieder (
    id integer NOT NULL AUTO_INCREMENT,
    anrede integer NOT NULL,
    vorname VARCHAR(50) NOT NULL,
    nachname VARCHAR(50) NOT NULL,
    strasse VARCHAR(100),
    hausnr VARCHAR(5),
    plz VARCHAR(5),
    ort VARCHAR(100),
    land VARCHAR(100),
    create_date DATETIME NOT NULL,
    geburtstag DATETIME DEFAULT NULL,
    position integer,
    PRIMARY KEY (`id`)
);

# --- !Downs

DROP TABLE mitglieder;
