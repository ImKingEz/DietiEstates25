ALTER TABLE annuncio
ADD COLUMN numero_visualizzazioni INT NOT NULL DEFAULT 0,
ADD COLUMN numero_offerte INT NOT NULL DEFAULT 0,
ADD COLUMN numero_visite_prenotate INT NOT NULL DEFAULT 0;