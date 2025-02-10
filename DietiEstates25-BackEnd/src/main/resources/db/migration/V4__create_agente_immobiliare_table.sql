CREATE TYPE genere AS ENUM ('maschio', 'femmina', 'non binario');

CREATE TABLE agente_immobiliare (
    id SERIAL PRIMARY KEY,
    id_agenzia INT,
    nome VARCHAR(255) NOT NULL,
    cognome VARCHAR(255) NOT NULL,
    data_di_nascita DATE NOT NULL,
    sesso genere NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    FOREIGN KEY (id_agenzia) REFERENCES agenzia_immobiliare(id)
);