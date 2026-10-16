-- Un libro guardado dos veces por el mismo usuario no tiene sentido, y sin
-- clave primaria la tabla lo permitia: solo lo evitaba el chequeo del service.
-- Primero se dejan las filas sin repetir, despues se agrega la clave.

CREATE TABLE usuario_libros_guardados_unicos AS
SELECT DISTINCT usuario_id, libro_id FROM usuario_libros_guardados;

DELETE FROM usuario_libros_guardados;

INSERT INTO usuario_libros_guardados (usuario_id, libro_id)
SELECT usuario_id, libro_id FROM usuario_libros_guardados_unicos;

DROP TABLE usuario_libros_guardados_unicos;

ALTER TABLE usuario_libros_guardados ADD PRIMARY KEY (usuario_id, libro_id);
