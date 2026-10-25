package com.bytebooks.api.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TurnstileClientTest {

    private static final String URL = "https://turnstile.test/siteverify";

    private MockRestServiceServer servidor;

    private TurnstileClient cliente(String claveSecreta) {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        return new TurnstileClient(builder.baseUrl(URL).build(), claveSecreta);
    }

    @Test
    @DisplayName("Sin clave secreta la verificacion esta apagada y no llama a Cloudflare")
    void sinClaveNoVerifica() {
        TurnstileClient turnstile = cliente("");

        assertThat(turnstile.valido(null, "1.2.3.4")).isTrue();
        servidor.verify();
    }

    @Test
    @DisplayName("Sin token se rechaza sin llamar a Cloudflare")
    void sinTokenSeRechaza() {
        TurnstileClient turnstile = cliente("secreto");

        assertThat(turnstile.valido(" ", "1.2.3.4")).isFalse();
        servidor.verify();
    }

    @Test
    @DisplayName("Un token valido pasa, y viajan el secreto, el token y la IP")
    void tokenValidoPasa() {
        TurnstileClient turnstile = cliente("secreto");
        servidor.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().formDataContains(Map.of(
                        "secret", "secreto",
                        "response", "token-bueno",
                        "remoteip", "1.2.3.4")))
                .andRespond(withSuccess("{\"success\":true,\"error-codes\":[]}", MediaType.APPLICATION_JSON));

        assertThat(turnstile.valido("token-bueno", "1.2.3.4")).isTrue();
        servidor.verify();
    }

    @Test
    @DisplayName("Un token que Cloudflare rechaza no pasa")
    void tokenRechazadoNoPasa() {
        TurnstileClient turnstile = cliente("secreto");
        servidor.expect(requestTo(URL))
                .andRespond(withSuccess("{\"success\":false,\"error-codes\":[\"timeout-or-duplicate\"]}",
                        MediaType.APPLICATION_JSON));

        assertThat(turnstile.valido("token-reusado", null)).isFalse();
    }

    @Test
    @DisplayName("Si Cloudflare falla se rechaza: ante la duda, cerrado")
    void cloudflareCaidoNoPasa() {
        TurnstileClient turnstile = cliente("secreto");
        servidor.expect(requestTo(URL)).andRespond(withServerError());

        assertThat(turnstile.valido("token", null)).isFalse();
    }
}
