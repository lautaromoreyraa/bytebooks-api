package com.bytebooks.api.config;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Import(OrigenPorPedido.class)
@ActiveProfiles("test")
@Testcontainers
@ExtendWith(SpringExtension.class)
public abstract class PruebaDeIntegracion {

    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:9")
            .withConfigurationOverride("mysql-de-test");

    static {
        MYSQL.start();
    }
}
