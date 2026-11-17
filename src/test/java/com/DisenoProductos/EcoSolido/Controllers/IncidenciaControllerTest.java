package com.DisenoProductos.EcoSolido.Controllers;

import com.DisenoProductos.EcoSolido.Middleware.GlobalExceptionHandler;
import com.DisenoProductos.EcoSolido.Models.DTOs.IncidenciaRequestDTO;
import com.DisenoProductos.EcoSolido.Services.IncidenciaService;
import com.DisenoProductos.EcoSolido.Services.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración del endpoint POST /incidencias/registrar (RF04):
 * la petición multipart pasa por la validación del DTO y el manejador global
 * de errores igual que en producción. El servicio es un doble (mock).
 */
@ExtendWith(MockitoExtension.class)
class IncidenciaControllerTest {

    private static final String FOTO = "https://res.cloudinary.com/demo/image/upload/foto.jpg";

    @Mock
    private IncidenciaService incidenciaService;

    @Mock
    private UsuarioService usuarioService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new IncidenciaController(incidenciaService, usuarioService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private MockMultipartFile incidencia(String json) {
        return new MockMultipartFile("incidencia", "", "application/json", json.getBytes(StandardCharsets.UTF_8));
    }

    private String json(String categoria, String coordenadas) {
        return "{"
                + "\"categoria\":\"" + categoria + "\","
                + "\"descripcion\":\"Bolsas de basura acumuladas en la esquina\","
                + "\"titulo\":\"Basura en la esquina\","
                + "\"direccionTexto\":\"Av. Arequipa 123, Lima\""
                + coordenadas
                + "}";
    }

    private static final String COORDENADAS_LIMA = ",\"latitud\":-12.0464,\"longitud\":-77.0428";

    @Test
    void registrar_incidenciaValida_devuelve200YLlamaAlServicio() throws Exception {
        mockMvc.perform(multipart("/incidencias/registrar")
                        .file(incidencia(json("Otro", COORDENADAS_LIMA)))
                        .param("urlsFotos", FOTO)
                        .principal(new UsernamePasswordAuthenticationToken("GASPER", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").exists());

        verify(incidenciaService).registrarIncidencia(any(IncidenciaRequestDTO.class), any(), eq(List.of(FOTO)), eq("GASPER"));
    }

    @Test
    void registrar_sinUbicacionEnElMapa_devuelve400SinLlamarAlServicio() throws Exception {
        mockMvc.perform(multipart("/incidencias/registrar")
                        .file(incidencia(json("Otro", "")))
                        .param("urlsFotos", FOTO)
                        .principal(new UsernamePasswordAuthenticationToken("GASPER", null)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Debe seleccionar la")));

        verifyNoInteractions(incidenciaService);
    }

    @Test
    void registrar_conCategoriaInventada_devuelve400SinLlamarAlServicio() throws Exception {
        mockMvc.perform(multipart("/incidencias/registrar")
                        .file(incidencia(json("Cualquier cosa", COORDENADAS_LIMA)))
                        .param("urlsFotos", FOTO)
                        .principal(new UsernamePasswordAuthenticationToken("GASPER", null)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("seleccionada no es")));

        verifyNoInteractions(incidenciaService);
    }

    @Test
    void registrar_sinFotos_devuelve400SinLlamarAlServicio() throws Exception {
        mockMvc.perform(multipart("/incidencias/registrar")
                        .file(incidencia(json("Otro", COORDENADAS_LIMA)))
                        .principal(new UsernamePasswordAuthenticationToken("GASPER", null)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("al menos una foto")));

        verifyNoInteractions(incidenciaService);
    }
}
