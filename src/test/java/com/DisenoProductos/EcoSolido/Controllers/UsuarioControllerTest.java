package com.DisenoProductos.EcoSolido.Controllers;

import com.DisenoProductos.EcoSolido.Middleware.GlobalExceptionHandler;
import com.DisenoProductos.EcoSolido.Models.DTOs.CrearUsuarioRequestDTO;
import com.DisenoProductos.EcoSolido.Models.DTOs.LoginResponseDTO;
import com.DisenoProductos.EcoSolido.Models.States.Rol;
import com.DisenoProductos.EcoSolido.Services.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración del controlador de usuarios: se llama a los endpoints
 * reales (/usuario/autenticar y /usuario/registrar) con peticiones HTTP simuladas,
 * pasando por la validación (@Valid) y el manejador global de errores.
 * El servicio se reemplaza por un doble (mock), así no se necesita base de datos.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UsuarioController(usuarioService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ---------- RF02: iniciar sesión ----------

    @Test
    void autenticar_conCredencialesValidas_devuelve200YElToken() throws Exception {
        when(usuarioService.autenticarUsuario("admin", "clave123"))
                .thenReturn(new LoginResponseDTO("token-de-prueba", "admin", Rol.ADMIN, 0));

        mockMvc.perform(post("/usuario/autenticar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"admin\",\"contrasena\":\"clave123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-de-prueba"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    void autenticar_conFormularioVacio_devuelve400SinLlamarAlServicio() throws Exception {
        mockMvc.perform(post("/usuario/autenticar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"\",\"contrasena\":\"clave123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("nombre de usuario es obligatorio")));

        verifyNoInteractions(usuarioService);
    }

    @Test
    void autenticar_conContrasenaIncorrecta_devuelve401() throws Exception {
        when(usuarioService.autenticarUsuario("admin", "otra-clave"))
                .thenThrow(new RuntimeException("Usuario o contrasena incorrectos"));

        mockMvc.perform(post("/usuario/autenticar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombreUsuario\":\"admin\",\"contrasena\":\"otra-clave\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- RF01: registrarse ----------

    private String registro(String nombre, String nombreUsuario) {
        return "{"
                + "\"nombreCompleto\":\"" + nombre + "\","
                + "\"apellidoCompleto\":\"Perez\","
                + "\"tipoDocumento\":\"DNI\","
                + "\"dni\":\"12345678\","
                + "\"telefono\":\"987654321\","
                + "\"correoElectronico\":\"juan@correo.com\","
                + "\"nombreUsuario\":\"" + nombreUsuario + "\","
                + "\"contrasena\":\"secreta123\","
                + "\"preguntaSeguridad\":\"Ciudad natal\","
                + "\"respuestaPregunta\":\"Lima\""
                + "}";
    }

    @Test
    void registrar_conDatosValidos_devuelve201() throws Exception {
        mockMvc.perform(post("/usuario/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registro("Juan", "juanperez")))
                .andExpect(status().isCreated());

        verify(usuarioService).registrarUsuario(any(CrearUsuarioRequestDTO.class));
    }

    @Test
    void registrar_conNombreConNumeros_devuelve400SinLlamarAlServicio() throws Exception {
        mockMvc.perform(post("/usuario/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registro("Ju4n", "juanperez")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("solo puede contener letras")));

        verifyNoInteractions(usuarioService);
    }

    @Test
    void registrar_conUsuarioConEspacios_devuelve400SinLlamarAlServicio() throws Exception {
        mockMvc.perform(post("/usuario/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registro("Juan", "mi usuario")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("sin espacios")));

        verifyNoInteractions(usuarioService);
    }
}
