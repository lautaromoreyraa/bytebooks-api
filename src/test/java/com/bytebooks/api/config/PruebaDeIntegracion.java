package com.bytebooks.api.config;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base de los tests de integracion.
 *
 * El contenedor es {@code static} y no lleva {@code @Container}, para que
 * Testcontainers lo arranque una sola vez y lo comparta entre todas las clases
 * que extiendan de aca. Uno por clase agregaria medio minuto de arranque a cada
 * archivo nuevo, y con el tiempo nadie corre la suite.
 *
 * La imagen se fija a la misma version que usa el compose del proyecto: un test
 * que pasa contra un motor distinto al de produccion no prueba lo que promete.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@ExtendWith(SpringExtension.class)
public abstract class PruebaDeIntegracion {

    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0");

    static {
        MYSQL.start();
    }
}
