CREATE TABLE agenzia_immobiliare (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE,
    partita_iva VARCHAR(20) NOT NULL,
    indirizzo_sede_legale VARCHAR(255),
    url_logo VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    data_creazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);