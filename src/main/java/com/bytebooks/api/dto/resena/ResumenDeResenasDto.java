package com.bytebooks.api.dto.resena;

/**
 * Promedio y cantidad de resenas de un libro, calculados por la base.
 *
 * Existe porque el cliente los deducia de la primera pagina de resenas: con mas
 * de diez, el promedio salia mal o directamente no se mostraba. AVG y COUNT ya
 * los tiene el motor sobre la tabla entera, asi que no hay razon para estimarlos
 * afuera.
 *
 * {@code promedio} es null cuando el libro no tiene ninguna resena: cero seria
 * un promedio, y "sin datos" no es lo mismo que "puntuacion cero".
 */
public record ResumenDeResenasDto(Double promedio, long total) { }
