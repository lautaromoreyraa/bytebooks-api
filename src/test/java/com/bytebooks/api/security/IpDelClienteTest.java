package com.bytebooks.api.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class IpDelClienteTest {

    private static MockHttpServletRequest pedido() {
        MockHttpServletRequest pedido = new MockHttpServletRequest();
        pedido.setRemoteAddr("10.0.0.1");
        // Lo que manda un atacante para saltear el limite: un X-Forwarded-For inventado.
        pedido.addHeader("X-Forwarded-For", "1.2.3.4, 203.0.113.9");
        return pedido;
    }

    @Test
    @DisplayName("detras de Cloudflare usa CF-Connecting-IP y no el X-Forwarded-For que manda el cliente")
    void detrasDeCloudflare() {
        MockHttpServletRequest pedido = pedido();
        pedido.addHeader("CF-Connecting-IP", "203.0.113.9");
        assertThat(new IpDelCliente(true).de(pedido)).isEqualTo("203.0.113.9");
    }

    @Test
    @DisplayName("sin el header de Cloudflare cae en la direccion de la conexion, nunca en X-Forwarded-For")
    void sinHeaderDeCloudflare() {
        assertThat(new IpDelCliente(true).de(pedido())).isEqualTo("10.0.0.1");
    }

    @Test
    @DisplayName("fuera de un proxy no confia en ningun header")
    void sinProxy() {
        MockHttpServletRequest pedido = pedido();
        pedido.addHeader("CF-Connecting-IP", "203.0.113.9");
        assertThat(new IpDelCliente(false).de(pedido)).isEqualTo("10.0.0.1");
    }
}
