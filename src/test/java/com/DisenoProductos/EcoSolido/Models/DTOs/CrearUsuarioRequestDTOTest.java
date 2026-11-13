package com.DisenoProductos.EcoSolido.Models.DTOs;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Criterios de aceptación de RF01 (registro de usuario) validados en el backend.
 */
class CrearUsuarioRequestDTOTest {

    private static ValidatorFactory fabrica;
    private static Validator validador;

    @BeforeAll
    static void crearValidador() {
        fabrica = Validation.buildDefaultValidatorFactory();
        validador = fabrica.getValidator();
    }

    @AfterAll
    static void cerrarValidador() {
        fabrica.close();
    }

    private CrearUsuarioRequestDTO usuarioValido() {
        CrearUsuarioRequestDTO dto = new CrearUsuarioRequestDTO();
        dto.setNombreCompleto("José Ángel");
        dto.setApellidoCompleto("Núñez Pérez");
        dto.setTipoDocumento("DNI");
        dto.setDni("12345678");
        dto.setTelefono("987654321");
        dto.setCorreoElectronico("jose@correo.com");
        dto.setNombreUsuario("jose.nunez");
        dto.setContrasena("secreta123");
        dto.setPreguntaSeguridad("¿En qué ciudad naciste?");
        dto.setRespuestaPregunta("Lima");
        return dto;
    }

    private Set<String> camposConError(CrearUsuarioRequestDTO dto) {
        return validador.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void usuarioCompleto_esValido() {
        assertTrue(camposConError(usuarioValido()).isEmpty());
    }

    @Test
    void nombreConNumeros_seRechaza() {
        CrearUsuarioRequestDTO dto = usuarioValido();
        dto.setNombreCompleto("Ju4n");
        assertEquals(Set.of("nombreCompleto"), camposConError(dto));
    }

    @Test
    void nombreDeUsuarioCortoOConEspacios_seRechaza() {
        CrearUsuarioRequestDTO corto = usuarioValido();
        corto.setNombreUsuario("ab");
        assertEquals(Set.of("nombreUsuario"), camposConError(corto));

        CrearUsuarioRequestDTO conEspacio = usuarioValido();
        conEspacio.setNombreUsuario("mi usuario");
        assertEquals(Set.of("nombreUsuario"), camposConError(conEspacio));
    }

    @Test
    void correoSinDominio_seRechaza() {
        CrearUsuarioRequestDTO dto = usuarioValido();
        dto.setCorreoElectronico("jose@correo");
        assertEquals(Set.of("correoElectronico"), camposConError(dto));
    }

    @Test
    void telefonoQueNoTiene9Digitos_seRechaza() {
        CrearUsuarioRequestDTO dto = usuarioValido();
        dto.setTelefono("98765");
        assertEquals(Set.of("telefono"), camposConError(dto));
    }

    @Test
    void contrasenaFueraDe6a72Caracteres_seRechaza() {
        CrearUsuarioRequestDTO corta = usuarioValido();
        corta.setContrasena("123");
        assertEquals(Set.of("contrasena"), camposConError(corta));

        CrearUsuarioRequestDTO larga = usuarioValido();
        larga.setContrasena("a".repeat(73));
        assertEquals(Set.of("contrasena"), camposConError(larga));
    }

    @Test
    void tipoDeDocumentoDesconocido_seRechaza() {
        CrearUsuarioRequestDTO dto = usuarioValido();
        dto.setTipoDocumento("LICENCIA");
        assertEquals(Set.of("tipoDocumento"), camposConError(dto));
    }
}
