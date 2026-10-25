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

/**
 * Verifica contra Cloudflare el token que deja el widget de Turnstile.
 *
 * La clave secreta no tiene default en application.properties: olvidarla en
 * produccion impide arrancar en vez de dejar el registro abierto. Vacia a
 * proposito, como en los tests, la verificacion queda apagada.
 */
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

    /** Los tests pasan un cliente atado al servidor simulado, sin pisarle la fabrica. */
    TurnstileClient(RestClient restClient, String claveSecreta) {
        this.restClient = restClient;
        this.claveSecreta = claveSecreta;
    }

    /**
     * Diez segundos y no los de por defecto: del otro lado hay alguien esperando
     * que se cree su cuenta.
     */
    private static SimpleClientHttpRequestFactory conTiempoLimite() {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(10));
        fabrica.setReadTimeout(Duration.ofSeconds(10));
        return fabrica;
    }

    /**
     * @param token el que entrego el widget; puede venir null
     * @param ip    la IP real de quien se registra, que Cloudflare usa como
     *              senal extra
     */
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
            // Ante la duda se rechaza: dejar pasar cuando Cloudflare no contesta
            // reabriria el registro en serie justo cuando nadie lo mira.
            log.warn("Turnstile no respondio: {}", e.getMessage());
            return false;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Respuesta(boolean success, @JsonProperty("error-codes") List<String> codigosDeError) { }
}
