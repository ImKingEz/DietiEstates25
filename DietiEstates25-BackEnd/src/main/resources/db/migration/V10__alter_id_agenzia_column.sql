DELETE FROM amministratore WHERE id_agenzia IS NULL;
ALTER TABLE amministratore
ALTER COLUMN id_agenzia SET NOT NULL;

DELETE FROM agente_immobiliare WHERE id_agenzia IS NULL;
ALTER TABLE agente_immobiliare
ALTER COLUMN id_agenzia SET NOT NULL;