package com.bytebooks.api.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;

@Component
public class TurnstileClient {

    private static final Logger log = LoggerFactory.getLogger(TurnstileClient.class);

    private final RestClient restClient;
    private final String claveSecreta;

    @Autowired
    public TurnstileClient(RestClient.Builder builder,
                           @Value("${app.turnstile.url}") String url,
                           @Value("${app.turnstile.clave-secreta}") String claveSecreta) {
        this(builder.baseUrl(url).requestFactory(conTiempoLimite()).build(), claveSecreta);
    }

    TurnstileClient(RestClient restClient, String claveSecreta) {
        this.restClient = restClient;
        this.claveSecreta = claveSecreta;
    }

    private static SimpleClientHttpRequestFactory conTiempoLimite() {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(10));
        fabrica.setReadTimeout(Duration.ofSeconds(10));
        return fabrica;
    }

    public boolean valido(String token, String ip) {
        if (claveSecreta == null || claveSecreta.isBlank()) {
            return true;
        }
        if (token == null || token.isBlank()) {
            return false;
        }

        MultiValueMap<String, String> formulario = new LinkedMultiValueMap<>();
        formulario.add("secret", claveSecreta);
        formulario.add("response", token);
        if (ip != null && !ip.isBlank()) {
            formulario.add("remoteip", ip);
        }

        try {
            Respuesta respuesta = restClient.post()
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formulario)
                    .retrieve()
                    .body(Respuesta.class);
            if (respuesta == null || !respuesta.success()) {
                log.info("Turnstile rechazo un registro: {}",
                        respuesta == null ? "sin respuesta" : respuesta.codigosDeError());
                return false;
            }
            return true;
        } catch (RestClientException e) {
            log.warn("Turnstile no respondio: {}", e.getMessage());
            return false;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Respuesta(boolean success, @JsonProperty("error-codes") List<String> codigosDeError) { }
}
