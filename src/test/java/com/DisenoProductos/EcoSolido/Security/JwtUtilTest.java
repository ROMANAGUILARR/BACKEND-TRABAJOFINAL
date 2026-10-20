package com.DisenoProductos.EcoSolido.Security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRETO = "clave-de-prueba-con-mas-de-32-caracteres-123456";

    private JwtUtil jwtUtil;

    @BeforeEach
    void preparar() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", SECRETO);
    }

    @Test
    void tokenGeneradoConservaUsuarioYRol() {
        String token = jwtUtil.generarToken("admin", "ADMIN");

        assertTrue(jwtUtil.validarToken(token));
        assertEquals("admin", jwtUtil.extraerNombreUsuario(token));
        assertEquals("ADMIN", jwtUtil.extraerRol(token));
    }

    @Test
    void rolNuloSeConvierteEnCiudadano() {
        String token = jwtUtil.generarToken("ana", null);

        assertEquals("CIUDADANO", jwtUtil.extraerRol(token));
    }

    @Test
    void tokenManipuladoNoEsValido() {
        String token = jwtUtil.generarToken("ana", "CIUDADANO");

        assertFalse(jwtUtil.validarToken(token + "x"));
        assertFalse(jwtUtil.validarToken("no-es-un-token"));
    }

    @Test
    void tokenFirmadoConOtraClaveNoEsValido() {
        String token = jwtUtil.generarToken("ana", "CIUDADANO");
        JwtUtil otro = new JwtUtil();
        ReflectionTestUtils.setField(otro, "SECRET_KEY", "otra-clave-distinta-con-mas-de-32-caracteres-999");

        assertFalse(otro.validarToken(token));
    }
}
