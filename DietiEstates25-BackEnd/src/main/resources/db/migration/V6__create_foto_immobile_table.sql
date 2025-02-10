CREATE TABLE foto_immobile (
    id SERIAL PRIMARY KEY,
    id_immobile INT NOT NULL,
    url VARCHAR(255) NOT NULL,
    FOREIGN KEY (id_immobile) REFERENCES immobile(id)
);