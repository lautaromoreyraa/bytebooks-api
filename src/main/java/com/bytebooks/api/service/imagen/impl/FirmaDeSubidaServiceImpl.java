package com.bytebooks.api.service.imagen.impl;

import com.bytebooks.api.dto.imagen.FirmaDeSubidaResponseDto;
import com.bytebooks.api.exception.ConfiguracionIncompletaException;
import com.bytebooks.api.service.imagen.FirmaDeSubidaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Firma subidas a Cloudinary para que el navegador suba directo, sin que el
 * archivo pase por la API y sin un upload preset abierto: con un preset sin
 * firma cualquiera que leyera el bundle podia subir archivos a la cuenta.
 */
@Service
public class FirmaDeSubidaServiceImpl implements FirmaDeSubidaService {

    private final String nombreDeLaNube;
    private final String apiKey;
    private final String apiSecret;
    private final String carpeta;

    public FirmaDeSubidaServiceImpl(@Value("${cloudinary.cloud-name}") String nombreDeLaNube,
                                    @Value("${cloudinary.api-key}") String apiKey,
                                    @Value("${cloudinary.api-secret}") String apiSecret,
                                    @Value("${cloudinary.carpeta}") String carpeta) {
        this.nombreDeLaNube = nombreDeLaNube;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.carpeta = carpeta;
    }

    @Override
    public FirmaDeSubidaResponseDto firmar() {
        if (nombreDeLaNube.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new ConfiguracionIncompletaException(
                    "Cloudinary no esta configurado en el servidor: faltan CLOUDINARY_CLOUD_NAME, "
                            + "CLOUDINARY_API_KEY o CLOUDINARY_API_SECRET");
        }
        long marcaDeTiempo = Instant.now().getEpochSecond();
        // Solo se firman la carpeta y la hora, y son lo unico que el navegador
        // puede mandar: si tambien eligiera el public_id, la firma le dejaria
        // pisar imagenes de otros. Cloudinary rechaza la firma pasada una hora.
        String aFirmar = "folder=" + carpeta + "&timestamp=" + marcaDeTiempo;
        return new FirmaDeSubidaResponseDto(sha1(aFirmar + apiSecret), marcaDeTiempo, apiKey, nombreDeLaNube, carpeta);
    }

    private static String sha1(String texto) {
        try {
            byte[] resumen = MessageDigest.getInstance("SHA-1").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumen);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("La JVM no tiene SHA-1", e);
        }
    }
}
