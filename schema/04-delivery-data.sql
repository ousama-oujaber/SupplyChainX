-- ========================================
-- DELIVERY MODULE - TEST DATA
-- ========================================
-- Enums: 
--   CustomerOrderStatus: EN_PREPARATION, EN_ROUTE, LIVREE
--   DeliveryStatus: PLANIFIEE, EN_COURS, LIVREE
-- ========================================

-- ========================================
-- CUSTOMERS
-- ========================================
INSERT INTO customers (name, address, city) VALUES 
('Restaurant Le Gourmet', '15 Rue de la Paix', 'Paris'),
('Hôtel Imperial', '45 Avenue des Champs-Élysées', 'Paris'),
('Café des Arts', '8 Place du Marché', 'Lyon'),
('Entreprise CleanTech', '120 Boulevard Industriel', 'Marseille'),
('Magasin ElectroPlus', '33 Rue Commerciale', 'Toulouse'),
('Chaîne Hôtelière Luxor', '78 Avenue Principale', 'Nice'),
('Centre Commercial Atlantis', '200 Route Nationale', 'Nantes'),
('Bureau TechCorp', '55 Rue Innovation', 'Bordeaux'),
('Hôpital Central', '90 Avenue Santé', 'Lille'),
('Université Sciences', '12 Campus Universitaire', 'Strasbourg');

-- ========================================
-- CUSTOMER ORDERS
-- ========================================
-- Status: EN_PREPARATION (Being prepared)
INSERT INTO customer_orders (customer_id, product_id, quantity, status) VALUES 
(1, 1, 2, 'EN_PREPARATION'),   -- Restaurant Le Gourmet - Machines à café
(2, 1, 5, 'EN_PREPARATION'),   -- Hôtel Imperial - Machines à café
(7, 6, 6, 'EN_PREPARATION'),   -- Centre Commercial Atlantis - Purificateurs
(2, 5, 4, 'EN_PREPARATION'),   -- Hôtel Imperial - Radiateurs
(8, 2, 3, 'EN_PREPARATION');   -- Bureau TechCorp - Robots de cuisine

-- Status: EN_ROUTE (In transit)
INSERT INTO customer_orders (customer_id, product_id, quantity, status) VALUES 
(3, 2, 3, 'EN_ROUTE'),         -- Café des Arts - Robots de cuisine
(4, 3, 2, 'EN_ROUTE'),         -- CleanTech - Aspirateurs
(9, 6, 4, 'EN_ROUTE'),         -- Hôpital Central - Purificateurs
(10, 4, 15, 'EN_ROUTE');       -- Université - Ventilateurs

-- Status: LIVREE (Delivered)
INSERT INTO customer_orders (customer_id, product_id, quantity, status) VALUES 
(5, 4, 10, 'LIVREE'),          -- ElectroPlus - Ventilateurs
(6, 5, 8, 'LIVREE'),           -- Luxor - Radiateurs
(1, 2, 1, 'LIVREE'),           -- Restaurant Le Gourmet - Robot de cuisine
(3, 1, 2, 'LIVREE'),           -- Café des Arts - Machines à café
(4, 4, 5, 'LIVREE');           -- CleanTech - Ventilateurs

-- ========================================
-- DELIVERIES
-- ========================================
-- Status: PLANIFIEE (Planned deliveries for EN_PREPARATION orders)
INSERT INTO deliveries (order_id, vehicle, driver, status, delivery_date, cost) VALUES 
(1, 'VAN-001', 'Jean Dupuis', 'PLANIFIEE', '2025-11-10', 25.50),
(2, 'TRUCK-102', 'Marie Leblanc', 'PLANIFIEE', '2025-11-12', 45.00),
(3, 'VAN-004', 'Thomas Rousseau', 'PLANIFIEE', '2025-11-15', 110.00),
(4, 'VAN-001', 'Jean Dupuis', 'PLANIFIEE', '2025-11-11', 35.00),
(5, 'VAN-003', 'Alain Petit', 'PLANIFIEE', '2025-11-13', 55.00);

-- Status: EN_COURS (In progress deliveries for EN_ROUTE orders)
INSERT INTO deliveries (order_id, vehicle, driver, status, delivery_date, cost) VALUES 
(6, 'VAN-003', 'Pierre Martin', 'EN_COURS', '2025-11-03', 75.00),
(7, 'TRUCK-201', 'Sophie Durand', 'EN_COURS', '2025-11-04', 120.00),
(8, 'VAN-005', 'Isabelle Blanc', 'EN_COURS', '2025-11-05', 95.00),
(9, 'TRUCK-103', 'François Roux', 'EN_COURS', '2025-11-06', 130.00);

-- Status: LIVREE (Completed deliveries for LIVREE orders)
INSERT INTO deliveries (order_id, vehicle, driver, status, delivery_date, cost) VALUES 
(10, 'VAN-002', 'Luc Bernard', 'LIVREE', '2025-10-28', 95.00),
(11, 'TRUCK-103', 'Claire Moreau', 'LIVREE', '2025-10-30', 85.00),
(12, 'VAN-001', 'Jean Dupuis', 'LIVREE', '2025-10-25', 20.00),
(13, 'VAN-004', 'Thomas Rousseau', 'LIVREE', '2025-10-27', 65.00),
(14, 'VAN-002', 'Luc Bernard', 'LIVREE', '2025-10-29', 45.00);
