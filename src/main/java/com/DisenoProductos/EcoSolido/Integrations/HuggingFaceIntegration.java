package com.DisenoProductos.EcoSolido.Integrations;

import com.DisenoProductos.EcoSolido.Models.DTOs.GenerarRecomendacionesRequest;
import com.DisenoProductos.EcoSolido.Services.HuggingFaceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class HuggingFaceIntegration {

    @Value("${huggingface.api.key}")
    private String apiKey;

    @Value("${huggingface.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-3.6-flash}")
    private String geminiModel;

    @Value("${gemini.fallback.model:gemini-3.1-flash-lite}")
    private String geminiFallbackModel;

    // Limite de 10MB para poder descargar fotos (el default de WebClient es 256KB)
    private final WebClient webClient = WebClient.builder()
            .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
            .build();

    private String llamarGemini(String modelo, Map<String, Object> body) {
        String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + modelo + ":generateContent";

        Map response = webClient.post()
                .uri(geminiUrl)
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", geminiApiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(60))
                .block();

        if (response == null) return null;
        List<Map> candidates = (List<Map>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) return null;
        Map content = (Map) candidates.get(0).get("content");
        if (content == null) return null;
        List<Map> responseParts = (List<Map>) content.get("parts");
        if (responseParts == null) return null;

        // Unir solo las partes de texto de la respuesta (se ignoran los "thoughts")
        StringBuilder texto = new StringBuilder();
        for (Map parte : responseParts) {
            if (Boolean.TRUE.equals(parte.get("thought"))) continue;
            Object t = parte.get("text");
            if (t != null) texto.append(t);
        }
        return texto.toString().trim();
    }

    public Map<String, String> describirYTitrular(List<String> imagenes) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new HuggingFaceException(
                    "GEMINI_API_KEY no configurado. Agrega la clave de Google Gemini en las variables de entorno.", null);
        }
        try {
            List<Map<String, Object>> parts = new ArrayList<>();

            parts.add(Map.of(
                    "text", "Analiza estas imagenes de una incidencia ambiental urbana. " +
                            "Responde EXACTAMENTE con este formato, sin texto adicional:\n" +
                            "TITULO: <titulo corto de maximo 6 palabras>\n" +
                            "DESCRIPCION: <descripcion de 2 oraciones sobre lo que ves en la imagen>"
            ));

            for (String entrada : imagenes) {
                if (entrada == null || entrada.isBlank()) continue;

                String base64Data;
                if (entrada.startsWith("data:image/")) {
                    int commaIndex = entrada.indexOf(",");
                    base64Data = entrada.substring(commaIndex + 1);
                } else if (entrada.startsWith("http://") || entrada.startsWith("https://")) {
                    byte[] imageBytes = webClient.get()
                            .uri(entrada)
                            .retrieve()
                            .bodyToMono(byte[].class)
                            .block();
                    base64Data = Base64.getEncoder().encodeToString(imageBytes);
                } else {
                    base64Data = entrada;
                }

                String mimeType = "image/jpeg";
                if (entrada.startsWith("data:image/")) {
                    mimeType = entrada.substring(5, entrada.indexOf(";"));
                } else if (entrada.toLowerCase().contains(".png")) {
                    mimeType = "image/png";
                } else if (entrada.toLowerCase().contains(".webp")) {
                    mimeType = "image/webp";
                }

                parts.add(Map.of(
                        "inline_data", Map.of(
                                "mimeType", mimeType,
                                "data", base64Data
                        )
                ));
            }

            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of("parts", parts)
                    ),
                    "generationConfig", Map.of(
                            // Margen amplio: los modelos Gemini 3 gastan tokens "pensando" antes de responder
                            "maxOutputTokens", 1024,
                            "temperature", 0.4,
                            "thinkingConfig", Map.of("thinkingLevel", "low")
                    )
            );

            String respuestaCompleta = null;
            Exception ultimoError = null;
            for (String modelo : List.of(geminiModel, geminiFallbackModel)) {
                if (modelo == null || modelo.isBlank()) continue;
                try {
                    respuestaCompleta = llamarGemini(modelo, body);
                    if (respuestaCompleta != null && !respuestaCompleta.isBlank()) break;
                } catch (Exception e) {
                    ultimoError = e;
                    System.err.println("Error Gemini (" + modelo + "): " + e.getMessage());
                }
            }
            if (respuestaCompleta == null || respuestaCompleta.isBlank()) {
                throw new IllegalStateException("La IA no devolvio texto", ultimoError);
            }

            String titulo = "";
            String descripcion = respuestaCompleta;

            if (respuestaCompleta.contains("TITULO:") && respuestaCompleta.contains("DESCRIPCION:")) {
                String[] partes = respuestaCompleta.split("DESCRIPCION:");
                String tituloParte = partes[0];
                titulo = tituloParte.replace("TITULO:", "").trim();
                if (titulo.length() > 60) {
                    titulo = titulo.substring(0, 60).trim();
                }
                descripcion = partes.length > 1 ? partes[1].trim() : respuestaCompleta;
            }

            return Map.of("descripcion", descripcion, "titulo", titulo);

        } catch (Exception e) {
            System.err.println("Error Gemini: " + e.getMessage());
            throw new HuggingFaceException("No se pudieron describir las fotos con la IA.", e);
        }
    }

    public List<String> generarRecomendaciones(GenerarRecomendacionesRequest request){
        String contextoExtra = (request.getContexto() != null && !request.getContexto().isBlank())
                ? " Contexto adicional: " + request.getContexto() + "."
                : "";
        try{
            Map<String, Object> body = Map.of(
                    "model", "Qwen/Qwen2-VL-7B-Instruct",
                    "max_tokens", 300,
                    "messages", List.of(
                            Map.of(
                                    "role", "user",
                                    "content", "Genera exactamente 2 recomendaciones practicas y diferentes " +
                                            "para un ciudadano de Lima sobre como manejar correctamente residuos de tipo: '" +
                                            request.getTipoMaterial() + "'." + contextoExtra +
                                            " Responde SOLO con un JSON con este formato exacto, sin texto adicional: " +
                                            "{\"recomendaciones\": [\"recomendacion1\", \"recomendacion2\"]}"
                            )
                    )
            );
            Map response = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            List<Map> choices = (List<Map>) response.get("choices");
            Map message = (Map) choices.get(0).get("message");
            String contenido = (String) message.get("content");
            String jsonLimpio = contenido
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();
            int inicio = jsonLimpio.indexOf('{');
            int fin = jsonLimpio.lastIndexOf('}') + 1;
            if (inicio >= 0 && fin > inicio) {
                jsonLimpio = jsonLimpio.substring(inicio, fin);
            }
            ObjectMapper mapper = new ObjectMapper();
            Map<String, List<String>> resultado = mapper.readValue(jsonLimpio, Map.class);
            return resultado.get("recomendaciones");
        } catch (Exception e) {
            throw new HuggingFaceException("No se pudieron generar las recomendaciones.", e);
        }
    }
}
