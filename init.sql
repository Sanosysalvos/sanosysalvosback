-- 0. Habilitar extensión para UUIDs
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. Definir tipos personalizados (Enums)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'pet_status') THEN
        CREATE TYPE pet_status AS ENUM ('PERDIDO', 'AVISTADO', 'RECUPERADO', 'RETIRADO');
    END IF;
END $$;

-- 2. Tabla de Usuarios (Microservicio ms-users)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    firebase_uid VARCHAR(128) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    rut VARCHAR(12) UNIQUE,
    email VARCHAR(100) UNIQUE NOT NULL,
    celular VARCHAR(20),
    direccion_residencia TEXT,
    is_admin BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tabla de Mascotas (Microservicio ms-pets)
CREATE TABLE IF NOT EXISTS pets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE, -- Relación con el dueño
    nombre VARCHAR(50) NOT NULL,
    especie VARCHAR(30) NOT NULL, -- Agregado para búsquedas rápidas
    color VARCHAR(30) NOT NULL,
    edad VARCHAR(20),
    descripcion TEXT,
    fecha_perdida DATE, -- Coincide con LocalDate en Java
    estado pet_status DEFAULT 'PERDIDO',
    latitud DECIMAL(10, 8), -- Renombrado para coincidir con el código Java
    longitud DECIMAL(11, 8), -- Renombrado para coincidir con el código Java
    foto TEXT, -- Para almacenar el Base64 inicial
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Tabla de Fotos (Para múltiples fotos por mascota)
CREATE TABLE IF NOT EXISTS pet_photos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pet_id UUID REFERENCES pets(id) ON DELETE CASCADE,
    photo_url TEXT NOT NULL, -- Puede ser URL o Base64
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Tabla de Ubicaciones (Para puntos de interés o zonas de búsqueda)
CREATE TABLE IF NOT EXISTS locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    descripcion_lugar TEXT,
    latitud DECIMAL(10, 8),
    longitud DECIMAL(11, 8),
    ciudad_comuna VARCHAR(100)
);

-- 6. Historial de Avistamientos
CREATE TABLE IF NOT EXISTS pet_sightings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pet_id UUID REFERENCES pets(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    latitud DECIMAL(10, 8),
    longitud DECIMAL(11, 8),
    fecha_avistamiento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    comentario_adicional TEXT,
    photo_url TEXT
);