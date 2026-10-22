package com.DisenoProductos.EcoSolido.Services;

import com.DisenoProductos.EcoSolido.Models.DTOs.CuadrillaRequestDTO;
import com.DisenoProductos.EcoSolido.Models.DTOs.CuadrillaResponseDTO;
import com.DisenoProductos.EcoSolido.Models.DTOs.TrabajadorRequestDTO;
import com.DisenoProductos.EcoSolido.Models.DTOs.TrabajadorResponseDTO;
import com.DisenoProductos.EcoSolido.Models.Entities.CuadrillaEntity;
import com.DisenoProductos.EcoSolido.Models.Entities.TrabajadorEntity;
import com.DisenoProductos.EcoSolido.Models.States.IncidenciaEstados;
import com.DisenoProductos.EcoSolido.Repositories.CuadrillaRepository;
import com.DisenoProductos.EcoSolido.Repositories.IncidenciaRepository;
import com.DisenoProductos.EcoSolido.Repositories.TrabajadorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Gestión de cuadrillas de limpieza y sus integrantes (solo ADMIN). */
@Service
public class CuadrillaService {
    private final CuadrillaRepository cuadrillaRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final IncidenciaRepository incidenciaRepository;
    @Value("${cuadrilla.max-incidencias-en-curso:3}")
    private int maxIncidenciasEnCurso;

    public CuadrillaService(CuadrillaRepository cuadrillaRepository,
                            TrabajadorRepository trabajadorRepository,
                            IncidenciaRepository incidenciaRepository) {
        this.cuadrillaRepository = cuadrillaRepository;
        this.trabajadorRepository = trabajadorRepository;
        this.incidenciaRepository = incidenciaRepository;
    }

    @Transactional(readOnly = true)
    public List<CuadrillaResponseDTO> listar() {
        Map<Integer, Map<IncidenciaEstados, Long>> carga = cargaPorCuadrilla();
        return cuadrillaRepository.findAllConTrabajadores().stream()
                .map(c -> aDTO(c, carga.getOrDefault(c.getIdCuadrilla(), Map.of())))
                .toList();
    }

    @Transactional
    public CuadrillaResponseDTO crear(CuadrillaRequestDTO dto) {
        String nombre = dto.nombre().trim();
        if (cuadrillaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe una cuadrilla llamada \"" + nombre + "\"");
        }
        CuadrillaEntity cuadrilla = new CuadrillaEntity();
        cuadrilla.setNombre(nombre);
        cuadrilla.setZona(limpiar(dto.zona()));
        cuadrilla.setActiva(dto.activa() == null || dto.activa());
        return aDTO(cuadrillaRepository.save(cuadrilla), Map.of());
    }

    @Transactional
    public CuadrillaResponseDTO actualizar(Integer idCuadrilla, CuadrillaRequestDTO dto) {
        CuadrillaEntity cuadrilla = buscarCuadrilla(idCuadrilla);
        String nombre = dto.nombre().trim();
        if (cuadrillaRepository.existsByNombreIgnoreCaseAndIdCuadrillaNot(nombre, idCuadrilla)) {
            throw new ConflictoException("Ya existe una cuadrilla llamada \"" + nombre + "\"");
        }
        cuadrilla.setNombre(nombre);
        cuadrilla.setZona(limpiar(dto.zona()));
        if (dto.activa() != null) {
            cuadrilla.setActiva(dto.activa());
        }
        return aDTO(cuadrillaRepository.save(cuadrilla), cargaPorCuadrilla().getOrDefault(idCuadrilla, Map.of()));
    }

    @Transactional
    public void eliminar(Integer idCuadrilla) {
        CuadrillaEntity cuadrilla = buscarCuadrilla(idCuadrilla);
        long asignadas = incidenciaRepository.countByCuadrilla_IdCuadrilla(idCuadrilla);
        if (asignadas > 0) {
            throw new ConflictoException("No se puede eliminar: la cuadrilla tiene " + asignadas
                    + " incidencia(s) asignada(s). Desactívala para que no reciba nuevas asignaciones.");
        }
        cuadrillaRepository.delete(cuadrilla);
    }

    @Transactional
    public TrabajadorResponseDTO agregarTrabajador(Integer idCuadrilla, TrabajadorRequestDTO dto) {
        CuadrillaEntity cuadrilla = buscarCuadrilla(idCuadrilla);
        TrabajadorEntity trabajador = new TrabajadorEntity();
        trabajador.setCuadrilla(cuadrilla);
        aplicarDatos(trabajador, dto);
        if (Boolean.TRUE.equals(trabajador.getEsJefe())) {
            quitarJefatura(idCuadrilla, null);
        }
        return aDTO(trabajadorRepository.save(trabajador));
    }

    @Transactional
    public TrabajadorResponseDTO actualizarTrabajador(Integer idTrabajador, TrabajadorRequestDTO dto) {
        TrabajadorEntity trabajador = buscarTrabajador(idTrabajador);
        aplicarDatos(trabajador, dto);
        if (Boolean.TRUE.equals(trabajador.getEsJefe())) {
            quitarJefatura(trabajador.getCuadrilla().getIdCuadrilla(), idTrabajador);
        }
        return aDTO(trabajadorRepository.save(trabajador));
    }

    @Transactional
    public void eliminarTrabajador(Integer idTrabajador) {
        TrabajadorEntity trabajador = buscarTrabajador(idTrabajador);
        trabajador.getCuadrilla().getTrabajadores().remove(trabajador);
        trabajadorRepository.delete(trabajador);
    }

    public CuadrillaEntity buscarCuadrilla(Integer idCuadrilla) {
        return cuadrillaRepository.findById(idCuadrilla)
                .orElseThrow(() -> new EntityNotFoundException("Cuadrilla no encontrada: " + idCuadrilla));
    }

    // ---- auxiliares ----

    private TrabajadorEntity buscarTrabajador(Integer idTrabajador) {
        return trabajadorRepository.findById(idTrabajador)
                .orElseThrow(() -> new EntityNotFoundException("Trabajador no encontrado: " + idTrabajador));
    }

    /** Solo puede haber un jefe por cuadrilla: al nombrar uno nuevo, los demás dejan de serlo. */
    private void quitarJefatura(Integer idCuadrilla, Integer excepto) {
        for (TrabajadorEntity t : trabajadorRepository.findByCuadrilla_IdCuadrillaAndEsJefeTrue(idCuadrilla)) {
            if (excepto == null || !t.getIdTrabajador().equals(excepto)) {
                t.setEsJefe(false);
                trabajadorRepository.save(t);
            }
        }
    }

    private void aplicarDatos(TrabajadorEntity trabajador, TrabajadorRequestDTO dto) {
        trabajador.setNombreCompleto(dto.nombreCompleto().trim());
        trabajador.setDni(limpiar(dto.dni()));
        trabajador.setTelefono(limpiar(dto.telefono()));
        trabajador.setEsJefe(Boolean.TRUE.equals(dto.esJefe()));
        trabajador.setActivo(dto.activo() == null || dto.activo());
    }

    private Map<Integer, Map<IncidenciaEstados, Long>> cargaPorCuadrilla() {
        Map<Integer, Map<IncidenciaEstados, Long>> carga = new HashMap<>();
        for (Object[] fila : incidenciaRepository.contarPorCuadrillaYEstado()) {
            Integer id = ((Number) fila[0]).intValue();
            IncidenciaEstados estado = (IncidenciaEstados) fila[1];
            long cantidad = ((Number) fila[2]).longValue();
            if (estado != null) {
                carga.computeIfAbsent(id, k -> new EnumMap<>(IncidenciaEstados.class)).put(estado, cantidad);
            }
        }
        return carga;
    }

    private CuadrillaResponseDTO aDTO(CuadrillaEntity c, Map<IncidenciaEstados, Long> carga) {
        List<TrabajadorResponseDTO> trabajadores = c.getTrabajadores().stream().map(this::aDTO).toList();
        return new CuadrillaResponseDTO(
                c.getIdCuadrilla(),
                c.getNombre(),
                c.getZona(),
                Boolean.TRUE.equals(c.getActiva()),
                trabajadores,
                carga.getOrDefault(IncidenciaEstados.PENDIENTE, 0L),
                carga.getOrDefault(IncidenciaEstados.EN_PROCESO, 0L),
                carga.getOrDefault(IncidenciaEstados.RESUELTO, 0L),
                maxIncidenciasEnCurso
        );
    }

    private TrabajadorResponseDTO aDTO(TrabajadorEntity t) {
        return new TrabajadorResponseDTO(
                t.getIdTrabajador(),
                t.getNombreCompleto(),
                t.getDni(),
                t.getTelefono(),
                Boolean.TRUE.equals(t.getEsJefe()),
                Boolean.TRUE.equals(t.getActivo())
        );
    }

    private static String limpiar(String valor) {
        if (valor == null) return null;
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }
}
