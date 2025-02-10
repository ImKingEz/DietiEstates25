CREATE TYPE tipo_vendita AS ENUM ('vendita', 'affitto');

CREATE TABLE immobile (
    id SERIAL PRIMARY KEY,
    id_agente INT NOT NULL,
    titolo VARCHAR(255) NOT NULL,
    descrizione TEXT NOT NULL,
    prezzo DECIMAL(15, 2) NOT NULL,
    dimensioni NUMERIC(10, 2) NOT NULL,
    indirizzo VARCHAR(255) NOT NULL,
    numero_stanze INT NOT NULL,
    piano INT,
    ascensore BOOLEAN DEFAULT FALSE,
    classe_energetica VARCHAR(5) NOT NULL,
    servizi TEXT,  -- Portineria, climatizzazione, ecc. (potrebbe essere una stringa separata da virgole o un JSON)
    tipo tipo_vendita NOT NULL,
    latitudine DECIMAL(9, 6) NOT NULL,
    longitudine DECIMAL(9, 6) NOT NULL,
    vicino_scuole BOOLEAN DEFAULT FALSE,
    vicino_parchi BOOLEAN DEFAULT FALSE,
    vicino_trasporto_pubblico BOOLEAN DEFAULT FALSE,
    data_inserimento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_agente) REFERENCES agente_immobiliare(id)
);