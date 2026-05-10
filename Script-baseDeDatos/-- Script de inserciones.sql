-- Script de inserciones

USE sistema_nexus;
 
-- =============================================================
--  MEMBRESÍAS
--  Solo 3 registros — uno por cada valor del ENUM
-- =============================================================
 
INSERT INTO membresias (nombre_membresia, precio_mensual, max_reservas, descripcion) VALUES
('ESSENTIAL', 29.99, 2,
 'Plan básico. Acceso a instalaciones con límite de 2 reservas activas simultáneas.'),
('VIP',       59.99, 4,
 'Plan intermedio. Acceso completo con límite de 4 reservas activas simultáneas.'),
('PREMIUM',   99.99, 0,
 'Plan premium. Acceso ilimitado a todas las instalaciones del club.');
 
-- =============================================================
--  INSTALACIONES
--  5 registros con tipos variados del ENUM
-- =============================================================
 
INSERT INTO instalaciones (nombre_instalacion, tipo_instalacion, capacidad, precio_hora, activa) VALUES
('Pista Pádel 1',   'PADEL',        4,  12.00, TRUE),
('Pista Pádel 2',   'PADEL',        4,  12.00, TRUE),
('Bar Principal',   'BAR',         30,   0.50, TRUE),
('Piscina Nexus',   'PISCINA',     50,   8.00, TRUE),
('Sala Fitness',    'GIMNASIO',    20,   6.00, TRUE);
 
-- =============================================================
--  CLIENTES
--  5 registros con datos realistas de España
--  Contraseña de login = email del cliente
-- =============================================================
 
INSERT INTO clientes (DNI, nombre, apellidos, email, telefono, fecha_nacimiento) VALUES
('12345678A', 'Juan',      'García López',      'juan.garcia@email.com',    '612-345-678', '1990-05-15'),
('23456789B', 'María',     'Martínez Ruiz',     'maria.martinez@email.com', '623-456-789', '1985-08-22'),
('34567890C', 'Carlos',    'Fernández Pérez',   'carlos.fernandez@email.com','634-567-890', '1992-11-30'),
('45678901D', 'Ana',       'López Sánchez',     'ana.lopez@email.com',      '645-678-901', '1988-03-10'),
('56789012E', 'Francisco', 'Rodríguez Gómez',   'francisco.rodriguez@email.com', NULL,    '1995-07-04');
 
-- =============================================================
--  SOCIOS
--  3 de los 5 clientes están dados de alta como socios
--  Los otros 2 son clientes no socios (para probar el login)
--
--  num_socio generado manualmente para los datos de prueba.
--  En producción lo genera el DAO automáticamente.
-- =============================================================
 
INSERT INTO socios (num_socio, DNI, fecha_alta, activo, id_membresia, referido_por) VALUES
('S-001', '12345678A', '2024-01-10', TRUE,  2, NULL),   -- Juan — VIP
('S-002', '23456789B', '2024-02-15', TRUE,  3, 'S-001'),-- María — PREMIUM, referida por Juan
('S-003', '34567890C', '2024-03-20', TRUE,  1, 'S-001');-- Carlos — ESSENTIAL, referido por Juan
 
-- =============================================================
--  RESUMEN DE ACCESOS AL SISTEMA
-- =============================================================
--
--  ADMINISTRADOR
--  Usuario:    admin
--  Contraseña: admin123
--
--  SOCIOS (usuario = DNI, contraseña = email)
--  S-001 → DNI: 12345678A  / Email: juan.garcia@email.com        (VIP)
--  S-002 → DNI: 23456789B  / Email: maria.martinez@email.com     (PREMIUM)
--  S-003 → DNI: 34567890C  / Email: carlos.fernandez@email.com   (ESSENTIAL)
--
--  CLIENTES NO SOCIOS (usuario = DNI, contraseña = email)
--  DNI: 45678901D  / Email: ana.lopez@email.com
--  DNI: 56789012E  / Email: francisco.rodriguez@email.com