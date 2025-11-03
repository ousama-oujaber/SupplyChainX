-- ========================================
-- USER MODULE - TEST DATA
-- ========================================
-- Enums: RoleUtilisateur
--   - ADMIN
--   - GESTIONNAIRE_APPROVISIONNEMENT
--   - RESPONSABLE_ACHATS
--   - SUPERVISEUR_LOGISTIQUE
--   - CHEF_PRODUCTION
--   - PLANIFICATEUR
--   - SUPERVISEUR_PRODUCTION
--   - GESTIONNAIRE_COMMERCIAL
--   - RESPONSABLE_LOGISTIQUE
--   - SUPERVISEUR_LIVRAISONS
-- ========================================

-- Admin User
INSERT INTO users (first_name, last_name, email, password, role) 
VALUES ('John', 'Admin', 'admin@supplychainx.com', '$2a$10$hash...', 'ADMIN');

-- Procurement Users
INSERT INTO users (first_name, last_name, email, password, role) 
VALUES ('Marie', 'Dupont', 'marie.dupont@supplychainx.com', '$2a$10$hash...', 'GESTIONNAIRE_APPROVISIONNEMENT');

INSERT INTO users (first_name, last_name, email, password, role) 
VALUES ('Pierre', 'Martin', 'pierre.martin@supplychainx.com', '$2a$10$hash...', 'RESPONSABLE_ACHATS');

-- Production Users
INSERT INTO users (first_name, last_name, email, password, role) 
VALUES ('Sophie', 'Bernard', 'sophie.bernard@supplychainx.com', '$2a$10$hash...', 'CHEF_PRODUCTION');

INSERT INTO users (first_name, last_name, email, password, role) 
VALUES ('Luc', 'Dubois', 'luc.dubois@supplychainx.com', '$2a$10$hash...', 'PLANIFICATEUR');

INSERT INTO users (first_name, last_name, email, password, role) 
VALUES ('Claire', 'Rousseau', 'claire.rousseau@supplychainx.com', '$2a$10$hash...', 'SUPERVISEUR_PRODUCTION');

-- Delivery/Logistics Users
INSERT INTO users (first_name, last_name, email, password, role) 
VALUES ('Marc', 'Leroy', 'marc.leroy@supplychainx.com', '$2a$10$hash...', 'SUPERVISEUR_LOGISTIQUE');

INSERT INTO users (first_name, last_name, email, password, role) 
VALUES ('Julie', 'Moreau', 'julie.moreau@supplychainx.com', '$2a$10$hash...', 'RESPONSABLE_LOGISTIQUE');

INSERT INTO users (first_name, last_name, email, password, role) 
VALUES ('Thomas', 'Simon', 'thomas.simon@supplychainx.com', '$2a$10$hash...', 'SUPERVISEUR_LIVRAISONS');

INSERT INTO users (first_name, last_name, email, password, role) 
VALUES ('Emma', 'Laurent', 'emma.laurent@supplychainx.com', '$2a$10$hash...', 'GESTIONNAIRE_COMMERCIAL');
