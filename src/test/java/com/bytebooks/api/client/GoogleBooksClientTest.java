package com.bytebooks.api.client;

import com.bytebooks.api.exception.GoogleBooksNoDisponibleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GoogleBooksClientTest {

    private static final String VOLUMENES = "https://www.googleapis.com/books/v1/volumes";
    private static final String UN_RESULTADO = "{\"items\":[{\"volumeInfo\":{\"title\":\"Rayuela\"}}]}";

    private MockRestServiceServer google;
    private GoogleBooksClient cliente;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder();
        google = MockRestServiceServer.bindTo(builder).build();
        cliente = new GoogleBooksClient(builder, Duration.ZERO);
        ReflectionTestUtils.setField(cliente, "apiKey", "clave-de-prueba");
    }

    @Test
    @DisplayName("un 503 suelto se reintenta y la búsqueda devuelve lo que trae el intento siguiente")
    void reintentaUn503() {
        google.expect(once(), requestTo(startsWith(VOLUMENES))).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        google.expect(once(), requestTo(startsWith(VOLUMENES))).andRespond(withSuccess(UN_RESULTADO, MediaType.APPLICATION_JSON));

        assertThat(cliente.buscar("rayuela", 5)).hasSize(1);
        google.verify();
    }

    @Test
    @DisplayName("si Google no contesta en ninguno de los tres intentos, avisa que no está disponible")
    void tresFallosSeguidos() {
        google.expect(times(3), requestTo(startsWith(VOLUMENES))).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> cliente.buscar("rayuela", 5)).isInstanceOf(GoogleBooksNoDisponibleException.class);
        google.verify();
    }

    @Test
    @DisplayName("una cuota agotada no se reintenta: otro pedido igual tampoco pasaría")
    void cuotaAgotadaSinReintento() {
        google.expect(once(), requestTo(startsWith(VOLUMENES))).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> cliente.buscar("rayuela", 5)).isInstanceOf(GoogleBooksNoDisponibleException.class);
        google.verify();
    }

    @Test
    @DisplayName("una respuesta sin items es una búsqueda sin resultados, no un error")
    void sinResultados() {
        google.expect(once(), requestTo(startsWith(VOLUMENES))).andRespond(withSuccess("{\"totalItems\":0}", MediaType.APPLICATION_JSON));

        assertThat(cliente.buscar("xyzzy", 5)).isEmpty();
        google.verify();
    }
}
