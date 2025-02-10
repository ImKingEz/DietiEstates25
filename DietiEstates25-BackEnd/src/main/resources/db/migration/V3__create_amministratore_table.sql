CREATE TABLE amministratore (
    id SERIAL PRIMARY KEY,
    id_agenzia INT,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    FOREIGN KEY (id_agenzia) REFERENCES agenzia_immobiliare(id)
);