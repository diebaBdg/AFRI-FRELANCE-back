-- =====================================================================
-- AfriFreelance — Seed base roles
-- =====================================================================

INSERT INTO td_roles (code, libelle, description, niveau_autorisation, actif)
VALUES
    ('ADMIN',      'Administrateur',  'Accès complet à toutes les fonctionnalités',      5, TRUE),
    ('INSTRUCTEUR','Instructeur',     'Vérifie et approuve les demandes',               3, TRUE),
    ('DEMANDEUR',  'Demandeur',       'Soumet des demandes d''agrément',                1, TRUE),
    ('SUPERVISEUR','Superviseur',     'Assigne les demandes aux instructeurs',          4, TRUE)
ON CONFLICT (code) DO NOTHING;
