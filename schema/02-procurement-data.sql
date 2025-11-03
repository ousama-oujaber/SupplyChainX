-- ========================================
-- PROCUREMENT MODULE - TEST DATA
-- ========================================
-- Enums: SupplyOrderStatus
--   - EN_ATTENTE (Waiting/Pending)
--   - EN_COURS (In Progress)
--   - RECUE (Received)
-- ========================================

-- ========================================
-- SUPPLIERS
-- ========================================
INSERT INTO suppliers (name, contact, rating, lead_time) VALUES 
('Acier France SA', 'contact@acierfrance.fr', 4.5, 7),
('Plastiques Européens', 'info@plastiques-eu.com', 4.0, 5),
('Composants Electroniques Inc', 'sales@electronics-inc.com', 4.8, 10),
('Métaux Premium', 'orders@metaux-premium.fr', 3.5, 14),
('Chimie Industrielle', 'contact@chimie-ind.fr', 4.2, 6);

-- ========================================
-- RAW MATERIALS
-- ========================================
-- Metals
INSERT INTO raw_materials (name, stock, stock_min, unit) VALUES 
('Acier inoxydable', 500, 100, 'kg'),
('Aluminium', 300, 80, 'kg'),
('Cuivre', 200, 50, 'kg');

-- Plastics
INSERT INTO raw_materials (name, stock, stock_min, unit) VALUES 
('Plastique ABS', 400, 100, 'kg'),
('Polypropylène', 250, 75, 'kg');

-- Electronics
INSERT INTO raw_materials (name, stock, stock_min, unit) VALUES 
('Circuit imprimé', 150, 30, 'pièce'),
('Condensateurs', 1000, 200, 'pièce'),
('Résistances', 2000, 500, 'pièce');

-- Chemicals
INSERT INTO raw_materials (name, stock, stock_min, unit) VALUES 
('Peinture époxy', 100, 20, 'litre'),
('Vernis protecteur', 80, 15, 'litre');

-- Other components
INSERT INTO raw_materials (name, stock, stock_min, unit) VALUES 
('Vis M6', 5000, 1000, 'pièce'),
('Écrous M6', 5000, 1000, 'pièce');

-- ========================================
-- MATERIAL-SUPPLIER ASSOCIATIONS (Many-to-Many)
-- ========================================
-- Acier France SA (supplier_id=1) supplies metals and hardware
INSERT INTO material_suppliers (material_id, supplier_id) VALUES 
(1, 1),  -- Acier inoxydable
(2, 1),  -- Aluminium
(3, 1),  -- Cuivre
(11, 1), -- Vis M6
(12, 1); -- Écrous M6

-- Plastiques Européens (supplier_id=2) supplies plastics
INSERT INTO material_suppliers (material_id, supplier_id) VALUES 
(4, 2),  -- Plastique ABS
(5, 2);  -- Polypropylène

-- Composants Electroniques Inc (supplier_id=3) supplies electronics
INSERT INTO material_suppliers (material_id, supplier_id) VALUES 
(6, 3),  -- Circuit imprimé
(7, 3),  -- Condensateurs
(8, 3);  -- Résistances

-- Métaux Premium (supplier_id=4) supplies metals (alternative) and hardware
INSERT INTO material_suppliers (material_id, supplier_id) VALUES 
(1, 4),  -- Acier inoxydable (alternative)
(3, 4),  -- Cuivre (alternative)
(11, 4), -- Vis M6 (alternative)
(12, 4); -- Écrous M6 (alternative)

-- Chimie Industrielle (supplier_id=5) supplies chemicals
INSERT INTO material_suppliers (material_id, supplier_id) VALUES 
(9, 5),  -- Peinture époxy
(10, 5); -- Vernis protecteur

-- ========================================
-- SUPPLY ORDERS
-- ========================================
-- Status: EN_ATTENTE (Pending order)
INSERT INTO supply_orders (supplier_id, order_date, status, expected_delivery_date) 
VALUES (1, '2025-10-25', 'EN_ATTENTE', '2025-11-08');

-- Status: EN_COURS (In progress order)
INSERT INTO supply_orders (supplier_id, order_date, status, expected_delivery_date) 
VALUES (2, '2025-10-28', 'EN_COURS', '2025-11-05');

-- Status: RECUE (Received/completed order)
INSERT INTO supply_orders (supplier_id, order_date, status, expected_delivery_date) 
VALUES (3, '2025-10-15', 'RECUE', '2025-10-30');

-- Status: EN_ATTENTE (Recent pending order)
INSERT INTO supply_orders (supplier_id, order_date, status, expected_delivery_date) 
VALUES (5, '2025-11-01', 'EN_ATTENTE', '2025-11-10');

-- Status: EN_COURS (In progress - urgent)
INSERT INTO supply_orders (supplier_id, order_date, status, expected_delivery_date) 
VALUES (4, '2025-10-29', 'EN_COURS', '2025-11-06');

-- ========================================
-- SUPPLY ORDER MATERIALS (Many-to-Many)
-- ========================================
-- Order 1 materials (Acier France - EN_ATTENTE)
INSERT INTO supply_order_materials (order_id, material_id) VALUES 
(1, 1),  -- Acier inoxydable
(1, 2),  -- Aluminium
(1, 11); -- Vis M6

-- Order 2 materials (Plastiques - EN_COURS)
INSERT INTO supply_order_materials (order_id, material_id) VALUES 
(2, 4),  -- Plastique ABS
(2, 5);  -- Polypropylène

-- Order 3 materials (Electronics - RECUE)
INSERT INTO supply_order_materials (order_id, material_id) VALUES 
(3, 6),  -- Circuit imprimé
(3, 7),  -- Condensateurs
(3, 8);  -- Résistances

-- Order 4 materials (Chemicals - EN_ATTENTE)
INSERT INTO supply_order_materials (order_id, material_id) VALUES 
(4, 9),  -- Peinture époxy
(4, 10); -- Vernis protecteur

-- Order 5 materials (Métaux Premium - EN_COURS)
INSERT INTO supply_order_materials (order_id, material_id) VALUES 
(5, 1),  -- Acier inoxydable
(5, 3),  -- Cuivre
(5, 12); -- Écrous M6
