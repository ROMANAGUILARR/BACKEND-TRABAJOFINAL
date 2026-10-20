package com.DisenoProductos.EcoSolido.Integrations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validación de DNI con ApiPeru (apiperu.dev / apiperu.pe).
 * El token va en la variable de entorno RENIEC_API_TOKEN.
 * Si no hay token o el servicio no responde, el registro no se bloquea.
 */
@Service
public class RENIECIntegration {
    @Value("${reniec.api.url:}")
    private String apiUrl;

    @Value("${reniec.api.token:}")
    private String apiToken;

    // Formatos conocidos de ApiPeru; se usa el primero que responda con datos
    private static final List<String> ENDPOINTS_APIPERU = List.of(
            "https://apiperu.dev/api/dni",
            "https://api.apiperu.dev/dni"
    );

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public boolean validarDni(String dni, String nombres, String apellidos) {
        if (apiToken == null || apiToken.isBlank() || apiToken.equalsIgnoreCase("dummy")) {
            System.out.println("RENIEC sin token configurado: se omite la validacion del DNI " + dni);
            return true;
        }
        String nombreReniec = consultarNombre(dni);
        if (nombreReniec == null) {
            System.out.println("RENIEC no devolvio datos para el DNI " + dni + ", se omite la validacion");
            return true;
        }
        Set<String> palabrasReniec = palabras(nombreReniec);
        Set<String> palabrasUsuario = palabras(nombres + " " + apellidos);
        // Coincide si todas las palabras escritas por el usuario aparecen en el nombre de RENIEC
        boolean coincide = !palabrasUsuario.isEmpty() && palabrasReniec.containsAll(palabrasUsuario);
        System.out.println("RENIEC DNI " + dni + ": " + (coincide ? "coincide" : "NO coincide") + " con el nombre ingresado");
        return coincide;
    }

    /** Devuelve el nombre completo según RENIEC, o null si no se pudo consultar. */
    private String consultarNombre(String dni) {
        List<String> endpoints = new ArrayList<>();
        if (apiUrl != null && !apiUrl.isBlank()) endpoints.add(apiUrl);
        for (String e : ENDPOINTS_APIPERU) if (!endpoints.contains(e)) endpoints.add(e);

        for (String endpoint : endpoints) {
            try {
                // Formato actual: POST con Bearer y {"dni": "..."}
                String nombre = leerNombre(enviar(HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("Authorization", "Bearer " + apiToken)
                        .timeout(Duration.ofSeconds(10))
                        .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(Map.of("dni", dni))))
                        .build(), endpoint));
                if (nombre != null) return nombre;

                // Formato antiguo: GET /dni/{numero}?api_token=...
                String token = URLEncoder.encode(apiToken, StandardCharsets.UTF_8);
                nombre = leerNombre(enviar(HttpRequest.newBuilder()
                        .uri(URI.create(endpoint + "/" + dni + "?api_token=" + token))
                        .header("Accept", "application/json")
                        .header("Authorization", "Bearer " + apiToken)
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build(), endpoint));
                if (nombre != null) return nombre;
            } catch (Exception e) {
                System.out.println("RENIEC " + endpoint + " no disponible: " + e.getMessage());
            }
        }
        return null;
    }

    private String enviar(HttpRequest request, String endpoint) throws Exception {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("RENIEC " + request.method() + " " + endpoint + " -> HTTP " + response.statusCode());
        return response.statusCode() == 200 ? response.body() : null;
    }

    /** Lee el nombre completo de la respuesta, acepte el formato que acepte ApiPeru. */
    private String leerNombre(String body) throws Exception {
        if (body == null || body.isBlank()) return null;
        JsonNode raiz = mapper.readTree(body);
        if (raiz.has("success") && !raiz.path("success").asBoolean(true)) return null;
        JsonNode data = raiz.has("data") && raiz.get("data").isObject() ? raiz.get("data") : raiz;

        for (String campo : List.of("nombre_completo", "nombreCompleto", "full_name")) {
            String valor = data.path(campo).asText("");
            if (!valor.isBlank()) return valor;
        }
        String nombres = data.path("nombres").asText("");
        String paterno = data.has("apellido_paterno") ? data.path("apellido_paterno").asText("") : data.path("apellidoPaterno").asText("");
        String materno = data.has("apellido_materno") ? data.path("apellido_materno").asText("") : data.path("apellidoMaterno").asText("");
        String completo = (nombres + " " + paterno + " " + materno).trim();
        return completo.isBlank() ? null : completo;
    }

    private Set<String> palabras(String texto) {
        Set<String> resultado = new HashSet<>();
        for (String p : quitarTildes(texto).toUpperCase().replace(",", " ").split("\\s+")) {
            if (!p.isBlank()) resultado.add(p.trim());
        }
        return resultado;
    }

    private String quitarTildes(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }
}
