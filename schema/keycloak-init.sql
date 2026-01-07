-- Create Keycloak database
CREATE DATABASE IF NOT EXISTS keycloak CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant privileges to scx_user for keycloak database
GRANT ALL PRIVILEGES ON keycloak.* TO 'scx_user'@'%';
FLUSH PRIVILEGES;
