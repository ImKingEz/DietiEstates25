ALTER TABLE immobile
    DROP COLUMN servizi;

ALTER TABLE immobile
    ADD COLUMN numero_bagni INT NOT NULL,
    ADD COLUMN portineria BOOLEAN DEFAULT FALSE,
    ADD COLUMN climatizzazione BOOLEAN DEFAULT FALSE;