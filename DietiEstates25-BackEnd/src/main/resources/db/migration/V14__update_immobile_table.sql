CREATE TABLE foto_immobile (
    id SERIAL PRIMARY KEY,
    id_annuncio INT NOT NULL,
    url VARCHAR(255) NOT NULL,
    FOREIGN KEY (id_annuncio) REFERENCES annuncio(id)
);