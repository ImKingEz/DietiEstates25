DROP TABLE foto_immobile;

DROP TABLE immobile;

CREATE TABLE immobile (
    id SERIAL PRIMARY KEY,
    dimensioni NUMERIC(10, 2) NOT NULL,
    indirizzo VARCHAR(255) NOT NULL,
    numero_camere INT NOT NULL,
    numero_bagni INT NOT NULL,
    piano INT,
    ascensore BOOLEAN DEFAULT FALSE,
    portineria BOOLEAN DEFAULT FALSE,
    climatizzazione BOOLEAN DEFAULT FALSE,
    classe_energetica VARCHAR(5) NOT NULL,
    tipologia VARCHAR(255) NOT NULL,
    latitudine DECIMAL(9, 6) NOT NULL,
    longitudine DECIMAL(9, 6) NOT NULL,
    vicino_scuole BOOLEAN DEFAULT FALSE,
    vicino_parchi BOOLEAN DEFAULT FALSE,
    vicino_trasporto_pubblico BOOLEAN DEFAULT FALSE
);