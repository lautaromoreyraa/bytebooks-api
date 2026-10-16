-- Esquema inicial: el que Hibernate creaba con ddl-auto, ahora en manos de Flyway.
--
-- Los nombres de las claves y restricciones son los que generó Hibernate a
-- propósito: la base de producción ya existe con esos nombres y Flyway la toma
-- como línea base (baseline-version=1) sin ejecutar este archivo. Una base
-- nueva queda idéntica a la de producción.

CREATE TABLE usuario (
  id            VARCHAR(36)  NOT NULL,
  apellido      VARCHAR(50)  NOT NULL,
  nombre        VARCHAR(50)  NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  email         VARCHAR(150) NOT NULL,
  descripcion   VARCHAR(500) DEFAULT NULL,
  foto_perfil   VARCHAR(500) DEFAULT NULL,
  rol           ENUM('ROLE_ADMIN','ROLE_MODERATOR','ROLE_USER') NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK5171l57faosmj8myawaucatdw (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE categoria (
  id          VARCHAR(36)   NOT NULL,
  descripcion VARCHAR(1000) DEFAULT NULL,
  nombre      VARCHAR(255)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK35t4wyxqrevf09uwx9e9p6o75 (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE libro (
  id               VARCHAR(36)   NOT NULL,
  titulo           VARCHAR(255)  NOT NULL,
  autor            VARCHAR(100)  NOT NULL,
  isbn             VARCHAR(13)   DEFAULT NULL,
  anio_publicacion VARCHAR(4)    DEFAULT NULL,
  editorial        VARCHAR(255)  DEFAULT NULL,
  descripcion      VARCHAR(1000) DEFAULT NULL,
  portada          VARCHAR(500)  DEFAULT NULL,
  estado_libro     ENUM('DISPONIBLE','OCULTO') DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UKqn9pauuvgqh324jmfru6wp6kj (titulo),
  UNIQUE KEY UKehuya6b4bxgkc4ru5wcf5njgr (isbn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE libro_categoria (
  categoria_id VARCHAR(36) NOT NULL,
  libro_id     VARCHAR(36) NOT NULL,
  PRIMARY KEY (categoria_id, libro_id),
  KEY FK11952p0wwxdk8rtrftctop2on (libro_id),
  CONSTRAINT FK11952p0wwxdk8rtrftctop2on FOREIGN KEY (libro_id) REFERENCES libro (id),
  CONSTRAINT FKj6mhf7vwwovtyqpcxj7r9nlvg FOREIGN KEY (categoria_id) REFERENCES categoria (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE resena (
  id           VARCHAR(36)   NOT NULL,
  libro_id     VARCHAR(36)   NOT NULL,
  usuario_id   VARCHAR(36)   NOT NULL,
  puntuacion   INT           NOT NULL,
  comentario   VARCHAR(2000) DEFAULT NULL,
  fecha_resena DATETIME(6)   NOT NULL,
  PRIMARY KEY (id),
  KEY FKha7vqfwuhiwxqunpg40f2nuqy (libro_id),
  KEY FK49q4gyungok45bbok1d7rpuxa (usuario_id),
  CONSTRAINT FK49q4gyungok45bbok1d7rpuxa FOREIGN KEY (usuario_id) REFERENCES usuario (id),
  CONSTRAINT FKha7vqfwuhiwxqunpg40f2nuqy FOREIGN KEY (libro_id) REFERENCES libro (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Sin clave primaria: la entidad la mapea como List, y Hibernate la creó así.
-- Agregarla es una migración aparte, después de limpiar duplicados si los hay.
CREATE TABLE usuario_libros_guardados (
  usuario_id VARCHAR(36) NOT NULL,
  libro_id   VARCHAR(36) NOT NULL,
  KEY FK6kkff26olbfv442weti5x9weh (libro_id),
  KEY FK3p9f5uds8456dtnwbbyde3y07 (usuario_id),
  CONSTRAINT FK3p9f5uds8456dtnwbbyde3y07 FOREIGN KEY (usuario_id) REFERENCES usuario (id),
  CONSTRAINT FK6kkff26olbfv442weti5x9weh FOREIGN KEY (libro_id) REFERENCES libro (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
