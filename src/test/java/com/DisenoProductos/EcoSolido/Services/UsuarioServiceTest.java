package com.DisenoProductos.EcoSolido.Services;

import com.DisenoProductos.EcoSolido.Integrations.RENIECIntegration;
import com.DisenoProductos.EcoSolido.Models.DTOs.ActualizarUsuarioDTO;
import com.DisenoProductos.EcoSolido.Models.DTOs.UsuarioListarDTO;
import com.DisenoProductos.EcoSolido.Models.Entities.UsuarioEntity;
import com.DisenoProductos.EcoSolido.Models.States.Rol;
import com.DisenoProductos.EcoSolido.Repositories.IncidenciaRepository;
import com.DisenoProductos.EcoSolido.Repositories.UsuarioInsigniaRepository;
import com.DisenoProductos.EcoSolido.Repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RENIECIntegration reniecIntegration;
    @Mock private IncidenciaRepository incidenciaRepository;
    @Mock private UsuarioInsigniaRepository usuarioInsigniaRepository;

    private UsuarioService service;

    @BeforeEach
    void preparar() {
        service = new UsuarioService(usuarioRepository, reniecIntegration);
        ReflectionTestUtils.setField(service, "incidenciaRepository", incidenciaRepository);
        ReflectionTestUtils.setField(service, "usuarioInsigniaRepository", usuarioInsigniaRepository);
    }

    private UsuarioEntity usuario(int id, String nombreUsuario) {
        UsuarioEntity u = new UsuarioEntity();
        u.setIdUsuario(id);
        u.setNombreCompleto("Ana");
        u.setApellidoCompleto("Quispe");
        u.setNombreUsuario(nombreUsuario);
        u.setTelefono("987654321");
        u.setCorreoElectronico("ana@correo.com");
        u.setRol(Rol.CIUDADANO);
        u.setPuntos(0);
        return u;
    }

    private ActualizarUsuarioDTO dto(String telefono, String correo) {
        ActualizarUsuarioDTO d = new ActualizarUsuarioDTO();
        d.setNombreCompleto("  Ana María ");
        d.setApellidoCompleto("Quispe Rojas");
        d.setTelefono(telefono);
        d.setCorreoElectronico(correo);
        return d;
    }

    @Test
    void actualizarUsuario_guardaLosNuevosDatos() {
        UsuarioEntity existente = usuario(1, "ana");
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(existente));
        when(usuarioRepository.findByCorreoElectronico("nuevo@correo.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByTelefono("999888777")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(incidenciaRepository.countByUsuario_NombreUsuario("ana")).thenReturn(3L);

        UsuarioListarDTO resultado = service.actualizarUsuario(1, dto("999888777", "nuevo@correo.com"));

        assertEquals("Ana María", resultado.getNombreCompleto());
        assertEquals("Quispe Rojas", resultado.getApellidoCompleto());
        assertEquals("999888777", resultado.getTelefono());
        assertEquals("nuevo@correo.com", resultado.getCorreoElectronico());
        assertEquals(3L, resultado.getTotalIncidencias());
        verify(usuarioRepository).save(existente);
    }

    @Test
    void actualizarUsuario_permiteConservarSuPropioCorreoYTelefono() {
        UsuarioEntity existente = usuario(1, "ana");
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(existente));
        when(usuarioRepository.findByCorreoElectronico("ana@correo.com")).thenReturn(Optional.of(existente));
        when(usuarioRepository.findByTelefono("987654321")).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(UsuarioEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> service.actualizarUsuario(1, dto("987654321", "ana@correo.com")));
    }

    @Test
    void actualizarUsuario_usuarioInexistente_lanzaNoEncontrado() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> service.actualizarUsuario(99, dto("999888777", "x@correo.com")));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void actualizarUsuario_correoDeOtroUsuario_lanzaConflicto() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario(1, "ana")));
        when(usuarioRepository.findByCorreoElectronico("luis@correo.com"))
                .thenReturn(Optional.of(usuario(2, "luis")));

        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> service.actualizarUsuario(1, dto("999888777", "luis@correo.com")));
        assertEquals("El correo electrónico ya está registrado", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void actualizarUsuario_telefonoDeOtroUsuario_lanzaConflicto() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario(1, "ana")));
        when(usuarioRepository.findByCorreoElectronico("ana@correo.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByTelefono("911222333")).thenReturn(Optional.of(usuario(2, "luis")));

        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> service.actualizarUsuario(1, dto("911222333", "ana@correo.com")));
        assertEquals("El teléfono ya está registrado", ex.getMessage());
    }

    @Test
    void eliminarUsuario_sinIncidencias_eliminaInsigniasYUsuario() {
        UsuarioEntity existente = usuario(1, "ana");
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(existente));
        when(incidenciaRepository.countByUsuario_NombreUsuario("ana")).thenReturn(0L);

        service.eliminarUsuario(1, "admin");

        verify(usuarioInsigniaRepository).deleteByUsuario_IdUsuario(1);
        verify(usuarioRepository).delete(existente);
    }

    @Test
    void eliminarUsuario_conIncidencias_lanzaConflictoYNoBorra() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario(1, "ana")));
        when(incidenciaRepository.countByUsuario_NombreUsuario("ana")).thenReturn(2L);

        assertThrows(ConflictoException.class, () -> service.eliminarUsuario(1, "admin"));
        verify(usuarioRepository, never()).delete(any());
        verifyNoInteractions(usuarioInsigniaRepository);
    }

    @Test
    void eliminarUsuario_propiaCuenta_lanzaConflicto() {
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario(1, "admin")));

        ConflictoException ex = assertThrows(ConflictoException.class,
                () -> service.eliminarUsuario(1, "admin"));
        assertEquals("No puedes eliminar tu propia cuenta", ex.getMessage());
        verify(usuarioRepository, never()).delete(any());
    }

    @Test
    void eliminarUsuario_inexistente_lanzaNoEncontrado() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.eliminarUsuario(99, "admin"));
    }
}
