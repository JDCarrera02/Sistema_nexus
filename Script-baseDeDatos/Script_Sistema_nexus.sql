-- Script creación del sistema_nexus

-- Creacion de la base de datos
CREATE DATABASE IF NOT EXISTS sistema_nexus
	-- Configuracion y tipo de codificacion
	CHARACTER SET utf8mb4	
	COLLATE utf8mb4_unicode_ci;
	
-- Usar o cambiar a la base de datos
USE sistema_nexus;

-- Creación de la tabla MEMBRESIAS
CREATE TABLE membresias(
	id_membresia INT AUTO_INCREMENT PRIMARY KEY,
	nombre_membresia ENUM('ESSENTIAL','VIP','PREMIUM') NOT NULL,
	precio_mensual DECIMAL(8,2),
	max_reservas INT NOT NULL DEFAULT 0,
	descripcion TEXT,
	-- Restricciones de tabla
	-- El precio no puede ser negativo
	CONSTRAINT ck_membresias_precio_mensual CHECK(precio_mensual >= 0),
	CONSTRAINT ck_membresias_reservas CHECK(max_reservas >= 0) -- El maximo de reservas no puede ser negativo
);

-- Creación de la tabla CLIENTES
CREATE TABLE clientes(
	dni CHAR(9) PRIMARY KEY,
	nombre VARCHAR(100) NOT NULL,
	apellidos VARCHAR(150) NOT NULL,
	email VARCHAR(150) NOT NULL UNIQUE, -- El email debe ser unico en todo el sistema
	telefono VARCHAR(15),
	fecha_nacimiento DATE
);

-- Creación de la tabla SOCIOS
CREATE TABLE socios(
	num_socio VARCHAR(20) NOT NULL PRIMARY KEY, -- Numero interno de socio, asignado por el sistema
	dni CHAR(9) NOT NULL UNIQUE, -- Clave heredada de CLIENTES, mismo DNI, distinto rol.
	fecha_alta DATE NOT NULL,
	activo BOOLEAN NOT NULL DEFAULT TRUE,
	id_membresia INT NOT NULL,
	referido_por VARCHAR(20),
	-- Restricciones de la tabla
	CONSTRAINT fk_socios_cliente FOREIGN KEY (dni) REFERENCES clientes(dni) ON DELETE CASCADE ON UPDATE CASCADE, 
	CONSTRAINT fk_socios_membresia FOREIGN KEY (id_membresia) REFERENCES membresias(id_membresia) ON DELETE RESTRICT ON UPDATE CASCADE,
	CONSTRAINT socios_referido_ FOREIGN KEY (referido_por) REFERENCES socios(num_socio) ON DELETE SET NULL ON UPDATE CASCADE
);

-- Creación de la tabla INSTALACIONES
CREATE TABLE instalaciones(
	id_instalacion INT AUTO_INCREMENT PRIMARY KEY,
	nombre_instalacion VARCHAR(100) NOT NULL UNIQUE,
	tipo_instalacion ENUM('BAR', 'SALON_EVENTOS', 'PISCINA', 'GIMNASIO', 'PADEL', 'BARBACOA') NOT NULL,
	capacidad INT NOT NULL,
	precio_hora DECIMAL(8,2) NOT NULL,
	activa BOOLEAN NOT NULL DEFAULT TRUE,
	-- Restricciones de la tabla
	CONSTRAINT ck_instalaciones_capacidad CHECK(capacidad > 0),
	CONSTRAINT ck_instalaciones_precio CHECK(precio_hora >= 0)
);

-- Creación de la tabla RESERVAS
CREATE TABLE reservas(
	id_reserva INT PRIMARY KEY AUTO_INCREMENT,
	dni_cliente CHAR(9) NOT NULL,
	id_instalacion INT NOT NULL,
	fecha_reserva DATE NOT NULL,
	hora_inicio TIME NOT NULL,
	hora_fin TIME NOT NULL,
	precio DECIMAL(8,2) NOT NULL,
	estado ENUM('CONFIRMADA', 'CANCELADA', 'COMPLETADA') NOT NULL DEFAULT 'CONFIRMADA',
	-- Restricciones de tabla
	CONSTRAINT fk_reservas_cliente FOREIGN KEY (dni_cliente) REFERENCES clientes (dni) ON DELETE RESTRICT ON UPDATE CASCADE,
	CONSTRAINT fk_reservas_instalacion  FOREIGN KEY (id_instalacion) REFERENCES instalaciones(id_instalacion) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT reservas_solapamiento_uq UNIQUE (id_instalacion, fecha_reserva, hora_inicio),
    CONSTRAINT reservas_horas_ck CHECK(hora_fin > hora_inicio),
    CONSTRAINT reservas_precio_ck CHECK(precio >= 0)
);







