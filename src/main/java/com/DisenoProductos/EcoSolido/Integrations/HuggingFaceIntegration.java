package com.DisenoProductos.EcoSolido.Integrations;

import com.DisenoProductos.EcoSolido.Models.DTOs.GenerarRecomendacionesRequest;
import com.DisenoProductos.EcoSolido.Services.HuggingFaceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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

    private final WebClient webClient = WebClient.create();

    @Cacheable(value="descripciones",key = "#imagenes.toString()")
    public Map<String, String> describirYTitrular(List<String> imagenes) {
        if (apiKey == null || apiKey.isBlank() || "dummy".equalsIgnoreCase(apiKey)) {
            throw new HuggingFaceException(
                    "HF_TOKEN no configurado. Agrega un token valido de Hugging Face en las variables de entorno.", null);
        }
        try {
            List<Map<String, Object>> content = new ArrayList<>();

            content.add(Map.of(
                    "type", "text",
                    "text", "Analiza estas imágenes de una incidencia ambiental urbana. " +
                            "Responde EXACTAMENTE con este formato, sin texto adicional:\n" +
                            "TITULO: <título corto de máximo 6 palabras>\n" +
                            "DESCRIPCION: <descripción de 2 oraciones sobre lo que ves en la imagen>"
            ));

            for (String entrada : imagenes) {
                if (entrada == null || entrada.isBlank()) continue;
                String dataUri;
                if (entrada.startsWith("data:image/")) {
                    dataUri = entrada;
                } else if (entrada.startsWith("http://") || entrada.startsWith("https://")) {
                    byte[] imageBytes = webClient.get()
                            .uri(entrada)
                            .retrieve()
                            .bodyToMono(byte[].class)
                            .block();
                    dataUri = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
                } else {
                    dataUri = "data:image/jpeg;base64," + entrada;
                }
                content.add(Map.of(
                        "type", "image_url",
                        "image_url", Map.of("url", dataUri)
                ));
            }

            Map<String, Object> body = Map.of(
                    "model", "meta-llama/Llama-3.2-11B-Vision-Instruct",
                    "messages", List.of(
                            Map.of("role", "user", "content", content)
                    )
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
            String respuestaCompleta = (String) message.get("content");

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
            if (e instanceof WebClientResponseException wce) {
                System.err.println("Error HF: " + wce.getResponseBodyAsString());
            }
            throw new HuggingFaceException("No se pudieron describir las fotos.", e);
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
