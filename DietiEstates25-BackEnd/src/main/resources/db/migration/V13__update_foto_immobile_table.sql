CREATE TABLE annuncio (
    id SERIAL PRIMARY KEY,
    id_agente INT NOT NULL,
    id_immobile INT NOT NULL,
    titolo VARCHAR(255) NOT NULL,
    descrizione TEXT NOT NULL,
    prezzo DECIMAL(15, 2) NOT NULL,
    tipo VARCHAR(255) NOT NULL,
    data_inserimento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_agente) REFERENCES agente_immobiliare(id),
    FOREIGN KEY (id_immobile) REFERENCES immobile(id)
);