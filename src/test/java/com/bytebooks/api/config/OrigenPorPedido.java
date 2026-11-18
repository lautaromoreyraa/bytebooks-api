package com.bytebooks.api.config;

import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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
