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
 * Criterios de aceptación de RF04 (registrar incidencia): el backend rechaza
 * los datos inválidos aunque alguien se salte las validaciones del frontend.
 */
class IncidenciaRequestDTOTest {

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

    private IncidenciaRequestDTO incidenciaValida() {
        IncidenciaRequestDTO dto = new IncidenciaRequestDTO();
        dto.setCategoria("Basura en vía pública");
        dto.setDescripcion("Bolsas de basura acumuladas en la esquina del parque.");
        dto.setTitulo("Basura en la esquina");
        dto.setLatitud(-12.0464);
        dto.setLongitud(-77.0428);
        dto.setDireccionTexto("Av. Arequipa 123, Lima");
        return dto;
    }

    private Set<String> camposConError(IncidenciaRequestDTO dto) {
        return validador.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void incidenciaCompleta_esValida() {
        assertTrue(camposConError(incidenciaValida()).isEmpty());
    }

    @Test
    void categoriaFueraDeLaLista_seRechaza() {
        IncidenciaRequestDTO dto = incidenciaValida();
        dto.setCategoria("Cualquier cosa");
        assertEquals(Set.of("categoria"), camposConError(dto));
    }

    @Test
    void lasSeisCategoriasDeLaLista_sonValidas() {
        String[] categorias = {
                "Acumulación y falta de recojo", "Basura en vía pública", "Contenedor dañado o lleno",
                "Escombros o materiales de construcción", "Residuos en parques o áreas verdes", "Otro"
        };
        for (String categoria : categorias) {
            IncidenciaRequestDTO dto = incidenciaValida();
            dto.setCategoria(categoria);
            assertTrue(camposConError(dto).isEmpty(), "Debería aceptar: " + categoria);
        }
    }

    @Test
    void sinUbicacionEnElMapa_seRechaza() {
        IncidenciaRequestDTO dto = incidenciaValida();
        dto.setLatitud(null);
        dto.setLongitud(null);
        assertEquals(Set.of("latitud", "longitud"), camposConError(dto));
    }

    @Test
    void coordenadasFueraDeRango_seRechazan() {
        IncidenciaRequestDTO dto = incidenciaValida();
        dto.setLatitud(95.0);
        dto.setLongitud(-200.0);
        assertEquals(Set.of("latitud", "longitud"), camposConError(dto));
    }

    @Test
    void descripcionVaciaOMayorA800Caracteres_seRechaza() {
        IncidenciaRequestDTO vacia = incidenciaValida();
        vacia.setDescripcion("   ");
        assertEquals(Set.of("descripcion"), camposConError(vacia));

        IncidenciaRequestDTO larga = incidenciaValida();
        larga.setDescripcion("a".repeat(801));
        assertEquals(Set.of("descripcion"), camposConError(larga));
    }

    @Test
    void direccionMayorA255Caracteres_seRechaza() {
        IncidenciaRequestDTO dto = incidenciaValida();
        dto.setDireccionTexto("a".repeat(256));
        assertEquals(Set.of("direccionTexto"), camposConError(dto));
    }
}
