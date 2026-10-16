package com.bytebooks.api.config;

import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Cada pedido de MockMvc sale desde una IP distinta.
 *
 * FiltroDeLimiteDePedidos cuenta por IP y todos los tests comparten el contexto: desde
 * 127.0.0.1 la suite entera consumia un solo cupo de 60 pedidos por minuto y,
 * segun lo rapido que corriera, algun test recibia 429 sin tener nada que ver
 * con el limite. Los tests que prueban el limite fijan su propia IP con
 * {@code .with(...)}, que se aplica despues de esta y la pisa.
 */
@TestConfiguration(proxyBeanMethods = false)
public class OrigenPorPedido {

    private static final AtomicInteger SIGUIENTE = new AtomicInteger();

    @Bean
    MockMvcBuilderCustomizer unaIpPorPedido() {
        return builder -> builder.defaultRequest(get("/").with(pedido -> {
            int n = SIGUIENTE.incrementAndGet();
            pedido.setRemoteAddr("10.%d.%d.%d".formatted((n >> 16) & 255, (n >> 8) & 255, n & 255));
            return pedido;
        }));
    }
}
