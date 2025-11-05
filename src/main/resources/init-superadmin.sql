-- Script para crear un usuario SUPERADMIN inicial
-- Ejecutar este script después de crear la base de datos

-- Password: superadmin123
-- El hash corresponde a la contraseña "superadmin123" usando BCrypt
INSERT INTO users (email, password, nombre, apellido, usuario, name, phone, role)
VALUES (
    'superadmin@haversack.com',
    '$2a$10$rVLJZ5X8jDnxLmvLH8JR3OXYxZ1qKQxZwE3wJZ5Y5Z5Z5Z5Z5Z5Z5u',
    'Super',
    'Admin',
    'superadmin',
    'Super Admin',
    '+1234567890',
    'SUPERADMIN'
);

-- Crear un usuario ADMIN de prueba
-- Password: admin123
INSERT INTO users (email, password, nombre, apellido, usuario, name, phone, role)
VALUES (
    'admin@haversack.com',
    '$2a$10$rVLJZ5X8jDnxLmvLH8JR3OXYxZ1qKQxZwE3wJZ5Y5Z5Z5Z5Z5Z5Z5u',
    'Admin',
    'User',
    'admin',
    'Admin User',
    '+1234567891',
    'ADMIN'
);

-- Crear un usuario USER regular de prueba
-- Password: user123
INSERT INTO users (email, password, nombre, apellido, usuario, name, phone, role)
VALUES (
    'user@haversack.com',
    '$2a$10$rVLJZ5X8jDnxLmvLH8JR3OXYxZ1qKQxZwE3wJZ5Y5Z5Z5Z5Z5Z5Z5u',
    'Regular',
    'User',
    'user',
    'Regular User',
    '+1234567892',
    'USER'
);

-- NOTA: 
-- Las contraseñas hasheadas en este script son ejemplos.
-- Para generar un hash BCrypt real, usa:
-- 1. Registra un usuario normal desde la aplicación
-- 2. Copia el hash de la base de datos
-- 3. Actualiza el rol manualmente: UPDATE users SET role = 'SUPERADMIN' WHERE email = 'tu@email.com';
-- O usa una herramienta online de BCrypt para generar el hash de tu contraseña deseada
