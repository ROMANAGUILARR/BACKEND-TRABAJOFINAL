package com.DisenoProductos.EcoSolido.Integrations;

import com.DisenoProductos.EcoSolido.Models.DTOs.GenerarRecomendacionesRequest;
import com.DisenoProductos.EcoSolido.Services.HuggingFaceException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

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

    @Value("${gemini.fallback2.model:gemini-3.5-flash}")
    private String geminiFallbackModel2;

    // Limite de 10MB para poder descargar fotos (el default de WebClient es 256KB)
    private final WebClient webClient = WebClient.builder()
            .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
            .build();

    private List<String> modelosGemini() {
        List<String> modelos = new ArrayList<>();
        for (String m : new String[]{geminiModel, geminiFallbackModel, geminiFallbackModel2}) {
            if (m != null && !m.isBlank() && !modelos.contains(m)) modelos.add(m);
        }
        return modelos;
    }

    private String llamarGemini(String modelo, Map<String, Object> body) {
        String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + modelo + ":generateContent";

        // Reintenta cuando Google esta saturado (503) o limita la cuota por minuto (429)
        Map response = webClient.post()
                .uri(geminiUrl)
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", geminiApiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(60))
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                        .filter(e -> e instanceof WebClientResponseException w
                                && (w.getStatusCode().value() == 503 || w.getStatusCode().value() == 429
                                    || w.getStatusCode().value() == 500))
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
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
            for (String modelo : modelosGemini()) {
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

    private static final int MIN_RECOMENDACIONES = 4;

    private String describirCategoria(String tipoMaterial) {
        String t = tipoMaterial == null ? "" : tipoMaterial.toLowerCase();
        if (t.contains("no aprovechable")) {
            return "residuos inorganicos NO aprovechables (no reciclables): papel higienico y servilletas usadas, " +
                    "panales, toallas higienicas, envolturas metalizadas, tecnopor sucio, colillas, residuos sanitarios. " +
                    "En Peru van en el contenedor NEGRO segun la NTP 900.058:2019";
        }
        if (t.contains("inorg")) {
            return "residuos inorganicos aprovechables (reciclables): papel, carton, botellas PET, plasticos rigidos, " +
                    "vidrio, latas y metales, tetrapak. En Peru van en el contenedor VERDE segun la NTP 900.058:2019";
        }
        if (t.contains("org")) {
            return "residuos organicos: restos de comida, cascaras de frutas y verduras, cafe, poda y hojas. " +
                    "En Peru van en el contenedor MARRON segun la NTP 900.058:2019 y se pueden compostar";
        }
        return "residuos solidos de tipo '" + tipoMaterial + "'";
    }

    private List<String> recomendacionesBase(String tipoMaterial) {
        String t = tipoMaterial == null ? "" : tipoMaterial.toLowerCase();
        if (t.contains("no aprovechable")) {
            return List.of(
                    "Separa papel higiénico, pañales, toallas higiénicas y envolturas metalizadas en un tacho con tapa y bolsa negra.",
                    "Envuelve los residuos sanitarios en papel o una bolsa pequeña antes de botarlos para evitar olores y contagios.",
                    "No mezcles estos residuos con los reciclables: un cartón manchado con grasa o comida ya no se puede reciclar.",
                    "Reduce su generación prefiriendo productos sin empaques metalizados ni tecnopor, y usando pañales o toallas reutilizables cuando sea posible.",
                    "Saca la bolsa bien cerrada solo en el horario del camión recolector de tu distrito para no atraer animales ni plagas."
            );
        }
        if (t.contains("inorg")) {
            return List.of(
                    "Separa papel, cartón, botellas PET, vidrio y latas en un tacho o bolsa blanca, aparte de los restos de comida.",
                    "Enjuaga y seca los envases antes de guardarlos: un residuo limpio vale más y no genera malos olores.",
                    "Aplasta botellas, latas y cajas de cartón para que ocupen menos espacio mientras las acumulas.",
                    "Reutiliza frascos de vidrio y bolsas antes de desecharlos, y evita los plásticos de un solo uso.",
                    "Entrega tus reciclables al programa de segregación de tu municipalidad o a un reciclador formalizado de tu zona."
            );
        }
        return List.of(
                "Separa cáscaras, restos de frutas y verduras, café y hojas en un recipiente con tapa exclusivo para orgánicos.",
                "Escurre bien los restos de comida y no agregues líquidos, para evitar malos olores y la aparición de moscas.",
                "Planifica tus compras y aprovecha sobras en nuevas comidas para reducir el desperdicio de alimentos.",
                "Haz compost en casa con tus orgánicos y hojas secas para obtener abono natural para tus plantas.",
                "Si no compostas, entrega tus orgánicos en bolsa marrón o al programa de valorización de orgánicos de tu municipalidad."
        );
    }

    private String promptRecomendaciones(GenerarRecomendacionesRequest request) {
        String contexto = request.getContexto() == null ? "" : request.getContexto().trim();
        return "Eres un educador ambiental de Lima, Peru. Genera entre 4 y 6 recomendaciones practicas, concretas " +
                "y diferentes entre si para que un ciudadano maneje correctamente " +
                describirCategoria(request.getTipoMaterial()) + ". " +
                "Cubre separacion en casa, almacenamiento, reduccion/reutilizacion y disposicion final o reciclaje. " +
                (contexto.isEmpty() ? "" :
                        "El ciudadano pide especificamente lo siguiente, prioriza responder a esto: \"" + contexto + "\". ") +
                "Cada recomendacion debe ser una sola oracion clara en espanol de maximo 35 palabras, sin numeracion. " +
                "Responde SOLO con JSON valido con este formato exacto: " +
                "{\"recomendaciones\": [\"...\", \"...\", \"...\", \"...\"]}";
    }

    private List<String> parsearRecomendaciones(String contenido) throws Exception {
        if (contenido == null) return List.of();
        String jsonLimpio = contenido.replaceAll("```json", "").replaceAll("```", "").trim();
        int inicio = jsonLimpio.indexOf('{');
        int fin = jsonLimpio.lastIndexOf('}') + 1;
        if (inicio >= 0 && fin > inicio) {
            jsonLimpio = jsonLimpio.substring(inicio, fin);
        }
        Map<String, Object> resultado = new ObjectMapper().readValue(jsonLimpio, Map.class);
        List<String> lista = new ArrayList<>();
        Object recs = resultado.get("recomendaciones");
        if (recs instanceof List<?> l) {
            for (Object o : l) {
                if (o != null && !o.toString().isBlank()) lista.add(o.toString().trim());
            }
        }
        return lista;
    }

    private List<String> recomendacionesConGemini(String prompt) throws Exception {
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "maxOutputTokens", 2048,
                        "temperature", 0.7,
                        "responseMimeType", "application/json",
                        "thinkingConfig", Map.of("thinkingLevel", "low")
                )
        );
        Exception ultimoError = null;
        for (String modelo : modelosGemini()) {
            if (modelo == null || modelo.isBlank()) continue;
            try {
                List<String> recs = parsearRecomendaciones(llamarGemini(modelo, body));
                if (recs.size() >= MIN_RECOMENDACIONES) return recs;
                ultimoError = new IllegalStateException("La IA devolvio solo " + recs.size() + " recomendaciones");
            } catch (Exception e) {
                ultimoError = e;
                System.err.println("Error Gemini recomendaciones (" + modelo + "): " + e.getMessage());
            }
        }
        throw ultimoError != null ? ultimoError : new IllegalStateException("Sin modelos Gemini configurados");
    }

    private List<String> recomendacionesConHuggingFace(String prompt) throws Exception {
        Map<String, Object> body = Map.of(
                "model", "Qwen/Qwen2.5-7B-Instruct",
                "max_tokens", 800,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );
        Map response = webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(60))
                .block();
        List<Map> choices = (List<Map>) response.get("choices");
        Map message = (Map) choices.get(0).get("message");
        return parsearRecomendaciones((String) message.get("content"));
    }

    public List<String> generarRecomendaciones(GenerarRecomendacionesRequest request) {
        String prompt = promptRecomendaciones(request);
        Exception error = null;

        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                return recomendacionesConGemini(prompt);
            } catch (Exception e) {
                error = e;
            }
        }
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                List<String> recs = recomendacionesConHuggingFace(prompt);
                if (!recs.isEmpty()) return recs;
            } catch (Exception e) {
                error = e;
                System.err.println("Error Hugging Face recomendaciones: " + e.getMessage());
            }
        }
        // Si ninguna IA responde, se entregan recomendaciones base de la categoria
        System.err.println("IA no disponible para recomendaciones, se usan las recomendaciones base: "
                + (error != null ? error.getMessage() : "sin GEMINI_API_KEY ni HF_TOKEN"));
        return recomendacionesBase(request.getTipoMaterial());
    }
}
