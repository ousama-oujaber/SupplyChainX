-- ========================================
-- PRODUCTION MODULE - TEST DATA
-- ========================================
-- Enums: ProductionOrderStatus
--   - EN_ATTENTE (Waiting/Pending)
--   - EN_PRODUCTION (In Production)
--   - TERMINE (Finished/Completed)
--   - BLOQUE (Blocked)
-- ========================================

-- ========================================
-- PRODUCTS
-- ========================================
INSERT INTO products (name, production_time, cost, stock) VALUES 
('Machine à café professionnelle', 12, 350.00, 25),
('Robot de cuisine multifonction', 8, 250.00, 40),
('Aspirateur industriel', 10, 400.00, 15),
('Ventilateur sur pied', 4, 80.00, 60),
('Radiateur électrique', 6, 150.00, 30),
('Purificateur d''air', 7, 200.00, 20);

-- ========================================
-- BILL OF MATERIALS
-- ========================================
-- Machine à café professionnelle (Product 1)
INSERT INTO bill_of_materials (product_id, material_id, quantity) VALUES 
(1, 1, 5),   -- Acier inoxydable: 5 kg
(1, 4, 2),   -- Plastique ABS: 2 kg
(1, 6, 2),   -- Circuit imprimé: 2 pièces
(1, 7, 10),  -- Condensateurs: 10 pièces
(1, 11, 20); -- Vis M6: 20 pièces

-- Robot de cuisine multifonction (Product 2)
INSERT INTO bill_of_materials (product_id, material_id, quantity) VALUES 
(2, 1, 3),   -- Acier inoxydable: 3 kg
(2, 4, 3),   -- Plastique ABS: 3 kg
(2, 6, 1),   -- Circuit imprimé: 1 pièce
(2, 7, 8),   -- Condensateurs: 8 pièces
(2, 11, 15); -- Vis M6: 15 pièces

-- Aspirateur industriel (Product 3)
INSERT INTO bill_of_materials (product_id, material_id, quantity) VALUES 
(3, 2, 4),   -- Aluminium: 4 kg
(3, 5, 5),   -- Polypropylène: 5 kg
(3, 6, 1),   -- Circuit imprimé: 1 pièce
(3, 3, 2),   -- Cuivre: 2 kg
(3, 11, 25); -- Vis M6: 25 pièces

-- Ventilateur sur pied (Product 4)
INSERT INTO bill_of_materials (product_id, material_id, quantity) VALUES 
(4, 2, 2),   -- Aluminium: 2 kg
(4, 5, 1),   -- Polypropylène: 1 kg
(4, 3, 1),   -- Cuivre: 1 kg
(4, 11, 10); -- Vis M6: 10 pièces

-- Radiateur électrique (Product 5)
INSERT INTO bill_of_materials (product_id, material_id, quantity) VALUES 
(5, 1, 6),   -- Acier inoxydable: 6 kg
(5, 3, 3),   -- Cuivre: 3 kg
(5, 6, 1),   -- Circuit imprimé: 1 pièce
(5, 9, 1),   -- Peinture époxy: 1 litre
(5, 11, 18); -- Vis M6: 18 pièces

-- Purificateur d'air (Product 6)
INSERT INTO bill_of_materials (product_id, material_id, quantity) VALUES 
(6, 2, 3),   -- Aluminium: 3 kg
(6, 4, 2),   -- Plastique ABS: 2 kg
(6, 6, 2),   -- Circuit imprimé: 2 pièces
(6, 7, 12),  -- Condensateurs: 12 pièces
(6, 11, 16); -- Vis M6: 16 pièces

-- ========================================
-- PRODUCTION ORDERS
-- ========================================
-- Status: EN_ATTENTE (Pending high priority order)
INSERT INTO production_orders (product_id, quantity, status, start_date, end_date, is_priority) 
VALUES (1, 10, 'EN_ATTENTE', '2025-11-05', NULL, TRUE);

-- Status: EN_PRODUCTION (In production normal priority)
INSERT INTO production_orders (product_id, quantity, status, start_date, end_date, is_priority) 
VALUES (2, 15, 'EN_PRODUCTION', '2025-10-30', NULL, FALSE);

-- Status: TERMINE (Completed order)
INSERT INTO production_orders (product_id, quantity, status, start_date, end_date, is_priority) 
VALUES (3, 5, 'TERMINE', '2025-10-20', '2025-10-28', FALSE);

-- Status: BLOQUE (Blocked order - missing materials)
INSERT INTO production_orders (product_id, quantity, status, start_date, end_date, is_priority) 
VALUES (5, 20, 'BLOQUE', '2025-10-25', NULL, TRUE);

-- Status: EN_ATTENTE (Pending normal priority)
INSERT INTO production_orders (product_id, quantity, status, start_date, end_date, is_priority) 
VALUES (4, 30, 'EN_ATTENTE', '2025-11-08', NULL, FALSE);

-- Status: EN_PRODUCTION (In production high priority)
INSERT INTO production_orders (product_id, quantity, status, start_date, end_date, is_priority) 
VALUES (6, 12, 'EN_PRODUCTION', '2025-11-01', NULL, TRUE);

-- Status: TERMINE (Another completed order)
INSERT INTO production_orders (product_id, quantity, status, start_date, end_date, is_priority) 
VALUES (4, 20, 'TERMINE', '2025-10-15', '2025-10-22', FALSE);

-- Status: EN_ATTENTE (Large pending order)
INSERT INTO production_orders (product_id, quantity, status, start_date, end_date, is_priority) 
VALUES (2, 25, 'EN_ATTENTE', '2025-11-10', NULL, TRUE);
