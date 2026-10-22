package com.DisenoProductos.EcoSolido.Services;

import com.DisenoProductos.EcoSolido.Integrations.CloudinaryIntegration;
import com.DisenoProductos.EcoSolido.Integrations.HuggingFaceIntegration;
import com.DisenoProductos.EcoSolido.Models.DTOs.ActualizarIncidenciaDTO;
import com.DisenoProductos.EcoSolido.Models.DTOs.HistorialResponseDTO;
import com.DisenoProductos.EcoSolido.Models.DTOs.IncidenciaRequestDTO;
import com.DisenoProductos.EcoSolido.Models.DTOs.MetricasResponseDTO;
import com.DisenoProductos.EcoSolido.Models.DTOs.SeguirIncidenciaResponseDTO;
import com.DisenoProductos.EcoSolido.Models.Entities.CuadrillaEntity;
import com.DisenoProductos.EcoSolido.Models.Entities.IncidenciaEntity;
import com.DisenoProductos.EcoSolido.Models.Entities.IncidenciaEvidenciaEntity;
import com.DisenoProductos.EcoSolido.Models.Entities.IncidenciaHistorialEntity;
import com.DisenoProductos.EcoSolido.Models.Entities.IncidenciaFotoEntity;
import com.DisenoProductos.EcoSolido.Models.Entities.InsigniaEntity;
import com.DisenoProductos.EcoSolido.Models.Entities.UsuarioEntity;
import com.DisenoProductos.EcoSolido.Models.States.IncidenciaEstados;
import com.DisenoProductos.EcoSolido.Repositories.CuadrillaRepository;
import com.DisenoProductos.EcoSolido.Repositories.IncidenciaHistorialRepository;
import com.DisenoProductos.EcoSolido.Repositories.IncidenciaRepository;
import org.springframework.security.access.AccessDeniedException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import com.DisenoProductos.EcoSolido.Repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IncidenciaService  {
    @Autowired
    private final IncidenciaRepository incidenciaRepository;
    @Autowired
    private final CloudinaryIntegration cloudinaryIntegration;
    @Autowired
    private final HuggingFaceIntegration huggingFaceIntegration;
    @Autowired
    public UsuarioRepository usuarioRepository;
    @Autowired
    public InsigniaService insigniaService;
    private final CuadrillaRepository cuadrillaRepository;
    private final IncidenciaHistorialRepository historialRepository;
    private static final int PUNTOS_POR_INCIDENCIA = 10;
    private static final int MAX_FOTOS_EVIDENCIA = 5;

    public IncidenciaService(IncidenciaRepository incidenciaRepository, CloudinaryIntegration cloudinaryIntegration, HuggingFaceIntegration huggingFaceIntegration, UsuarioRepository usuarioRepository, InsigniaService insigniaService,
                             CuadrillaRepository cuadrillaRepository, IncidenciaHistorialRepository historialRepository) {
        this.incidenciaRepository = incidenciaRepository;
        this.cloudinaryIntegration = cloudinaryIntegration;
        this.huggingFaceIntegration = huggingFaceIntegration;
        this.usuarioRepository = usuarioRepository;
        this.insigniaService = insigniaService;
        this.cuadrillaRepository = cuadrillaRepository;
        this.historialRepository = historialRepository;
    }

    public int getPuntosPorIncidencia() {
        return PUNTOS_POR_INCIDENCIA;
    }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        int R = 6371000;
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    @Transactional
    public List<InsigniaEntity> registrarIncidencia(IncidenciaRequestDTO incidenciaDTO,
                                                      List<MultipartFile> fotos,
                                                      List<String> urlsFotos, String nombreUsuario) throws IOException {
        boolean tieneUrls = urlsFotos != null && !urlsFotos.isEmpty();
        boolean tieneArchivos = fotos != null && !fotos.isEmpty() && fotos.stream().anyMatch(f -> !f.isEmpty());

        if (!tieneUrls && !tieneArchivos) {
            throw new IllegalStateException("Debe adjuntar al menos 1 foto");
        }
        UsuarioEntity usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<IncidenciaEntity> posiblesDuplicados = incidenciaRepository.findByUsuario_NombreUsuarioAndCategoriaAndDescripcion(
                nombreUsuario,
                incidenciaDTO.getCategoria(),
                incidenciaDTO.getDescripcion()
        );
        for (IncidenciaEntity prev : posiblesDuplicados) {
            double distancia = calcularDistancia(prev.getLatitud(), prev.getLongitud(), incidenciaDTO.getLatitud(), incidenciaDTO.getLongitud());
            if (distancia <= 50.0) {
                throw new DuplicateIncidentException("Esta incidencia ya fue registrada anteriormente. No se asignaran puntos adicionales.");
            }
        }

        IncidenciaEntity incidencia = new IncidenciaEntity();
        incidencia.setDescripcion(incidenciaDTO.getDescripcion());
        incidencia.setCategoria(incidenciaDTO.getCategoria());
        incidencia.setUsuario(usuario);

        String titulo;
        if (incidenciaDTO.getTitulo() != null && !incidenciaDTO.getTitulo().isBlank()) {
            titulo = incidenciaDTO.getTitulo();
        } else if (incidenciaDTO.getDescripcion() != null && incidenciaDTO.getDescripcion().trim().length() > 5) {
            titulo = "Incidencia #" + incidenciaDTO.getCategoria();
        } else {
            titulo = "Incidencia #" + incidenciaDTO.getCategoria();
        }
        incidencia.setTitulo(titulo);

        List<IncidenciaFotoEntity> fotosEntidad = new ArrayList<>();

        if (urlsFotos != null && !urlsFotos.isEmpty()) {
            for (String url : urlsFotos) {
                IncidenciaFotoEntity fotoEntidad = new IncidenciaFotoEntity();
                fotoEntidad.setUrlFoto(url);
                fotoEntidad.setIncidencia(incidencia);
                fotosEntidad.add(fotoEntidad);
            }
        } else {
            for (MultipartFile foto : fotos) {
                Map resultado = cloudinaryIntegration.subir(foto);
                IncidenciaFotoEntity fotoEntidad = new IncidenciaFotoEntity();
                fotoEntidad.setUrlFoto((String) resultado.get("secure_url"));
                fotoEntidad.setPublicId((String) resultado.get("public_id"));
                fotoEntidad.setIncidencia(incidencia);
                fotosEntidad.add(fotoEntidad);
            }
        }
        incidencia.setFotos(fotosEntidad);
        incidencia.setEstado(IncidenciaEstados.PENDIENTE);
        incidencia.setLatitud(incidenciaDTO.getLatitud());
        incidencia.setLongitud(incidenciaDTO.getLongitud());
        incidencia.setDireccionTexto(incidenciaDTO.getDireccionTexto());
        incidenciaRepository.save(incidencia);

        sincronizarPuntos(usuario);

        return insigniaService.evaluarYDesbloquearInsignias(nombreUsuario);
    }

    public Map<String, String> generarDescripcionYTitulo(List<String> urlFotos){
        if (urlFotos == null || urlFotos.isEmpty()) {
            return Map.of("descripcion", "No se proporcionaron imagenes.", "titulo", "");
        }
        try {
            return huggingFaceIntegration.describirYTitrular(urlFotos);
        } catch (Exception e) {
            throw new HuggingFaceException("No se pudo describir la foto.", e);
        }
    }

    public List<String> subirFotos(List<MultipartFile> fotos) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile foto : fotos) {
            Map resultado = cloudinaryIntegration.subir(foto);
            String secureUrl = (String) resultado.get("secure_url");
            urls.add(secureUrl);
        }
        return urls;
    }

    @Transactional
    public List<SeguirIncidenciaResponseDTO> mostrarIncidencias(String nombreUsuario){
        List<IncidenciaEntity> incidencias=incidenciaRepository.findByUsuario_NombreUsuarioConFotos(nombreUsuario);
        return incidencias.stream().distinct().map(this::aSeguirIncidenciaDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<SeguirIncidenciaResponseDTO> mostrarIncidenciasTotales(){
        List<IncidenciaEntity> incidencias=incidenciaRepository.findAllConFotos();
        return incidencias.stream().distinct().map(this::aSeguirIncidenciaDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<SeguirIncidenciaResponseDTO> mostrarIncidenciasPorUsuario(Integer idUsuario){
        List<IncidenciaEntity> incidencias=incidenciaRepository.findByUsuario_IdUsuarioConFotos(idUsuario);
        return incidencias.stream().distinct().map(this::aSeguirIncidenciaDTO).collect(Collectors.toList());
    }

    @Transactional
    public SeguirIncidenciaResponseDTO cambiarEstado(Integer idIncidencia, IncidenciaEstados estado, String realizadoPor){
        IncidenciaEntity incidencia = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new EntityNotFoundException("Incidencia no encontrada: " + idIncidencia));

        if (estado == null) {
            throw new IllegalArgumentException("Debe indicar el nuevo estado de la incidencia");
        }
        aplicarCambioEstado(incidencia, estado, null, realizadoPor);

        IncidenciaEntity guardada = incidenciaRepository.save(incidencia);
        return aSeguirIncidenciaDTO(guardada);
    }

    @Transactional
    public SeguirIncidenciaResponseDTO actualizarIncidencia(Integer idIncidencia, ActualizarIncidenciaDTO dto, String realizadoPor) {
        IncidenciaEntity incidencia = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new EntityNotFoundException("Incidencia no encontrada: " + idIncidencia));

        incidencia.setTitulo(dto.getTitulo() != null && !dto.getTitulo().isBlank()
                ? dto.getTitulo() : incidencia.getTitulo());
        incidencia.setDescripcion(dto.getDescripcion());
        incidencia.setCategoria(dto.getCategoria());
        incidencia.setDireccionTexto(dto.getDireccionTexto());
        incidencia.setLatitud(dto.getLatitud());
        incidencia.setLongitud(dto.getLongitud());
        if (dto.getEstado() != null) {
            aplicarCambioEstado(incidencia, dto.getEstado(), null, realizadoPor);
        }

        IncidenciaEntity guardada = incidenciaRepository.save(incidencia);
        return aSeguirIncidenciaDTO(guardada);
    }

    // ---------- Atención por cuadrillas ----------

    /** Asigna (o quita, con idCuadrilla = null) la cuadrilla responsable. Si estaba PENDIENTE pasa a EN_PROCESO. */
    @Transactional
    public SeguirIncidenciaResponseDTO asignarCuadrilla(Integer idIncidencia, Integer idCuadrilla, String realizadoPor) {
        IncidenciaEntity incidencia = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new EntityNotFoundException("Incidencia no encontrada: " + idIncidencia));
        CuadrillaEntity anterior = incidencia.getCuadrilla();

        if (idCuadrilla == null) {
            if (anterior == null) {
                return aSeguirIncidenciaDTO(incidencia);
            }
            incidencia.setCuadrilla(null);
            incidencia.setFechaAsignacion(null);
            registrarHistorial(incidencia, IncidenciaHistorialEntity.TIPO_ASIGNACION, null, null,
                    "Se quitó la asignación a la cuadrilla " + anterior.getNombre(), realizadoPor);
            return aSeguirIncidenciaDTO(incidenciaRepository.save(incidencia));
        }

        CuadrillaEntity cuadrilla = cuadrillaRepository.findById(idCuadrilla)
                .orElseThrow(() -> new EntityNotFoundException("Cuadrilla no encontrada: " + idCuadrilla));
        if (!Boolean.TRUE.equals(cuadrilla.getActiva())) {
            throw new IllegalArgumentException("La cuadrilla \"" + cuadrilla.getNombre() + "\" está desactivada y no puede recibir incidencias.");
        }
        if (anterior != null && anterior.getIdCuadrilla().equals(cuadrilla.getIdCuadrilla())) {
            return aSeguirIncidenciaDTO(incidencia);
        }
        if (incidencia.getEstado() == IncidenciaEstados.RESUELTO) {
            throw new IllegalArgumentException("La incidencia ya está resuelta; no se puede reasignar.");
        }

        incidencia.setCuadrilla(cuadrilla);
        incidencia.setFechaAsignacion(LocalDateTime.now());
        String detalle = (anterior == null ? "Asignada a la cuadrilla " : "Reasignada de " + anterior.getNombre() + " a la cuadrilla ")
                + cuadrilla.getNombre() + (cuadrilla.getZona() != null ? " (" + cuadrilla.getZona() + ")" : "");
        registrarHistorial(incidencia, IncidenciaHistorialEntity.TIPO_ASIGNACION, null, null, detalle, realizadoPor);

        if (incidencia.getEstado() == IncidenciaEstados.PENDIENTE) {
            aplicarCambioEstado(incidencia, IncidenciaEstados.EN_PROCESO, "La cuadrilla se encarga de la atención", realizadoPor);
        }
        return aSeguirIncidenciaDTO(incidenciaRepository.save(incidencia));
    }

    /** Marca la incidencia como RESUELTA adjuntando fotos del trabajo terminado. */
    @Transactional
    public SeguirIncidenciaResponseDTO resolverConEvidencia(Integer idIncidencia, List<MultipartFile> fotos,
                                                           String comentario, String realizadoPor) throws IOException {
        IncidenciaEntity incidencia = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new EntityNotFoundException("Incidencia no encontrada: " + idIncidencia));
        List<MultipartFile> archivos = fotos == null ? List.of()
                : fotos.stream().filter(f -> f != null && !f.isEmpty()).toList();

        if (incidencia.getEstado() == IncidenciaEstados.RESUELTO) {
            throw new IllegalArgumentException("La incidencia ya está resuelta.");
        }
        if (incidencia.getCuadrilla() == null) {
            throw new IllegalArgumentException("Asigna una cuadrilla antes de marcar la incidencia como resuelta.");
        }
        if (archivos.isEmpty()) {
            throw new IllegalArgumentException("Adjunta al menos una foto de evidencia del trabajo terminado.");
        }
        if (archivos.size() > MAX_FOTOS_EVIDENCIA) {
            throw new IllegalArgumentException("Puedes adjuntar como máximo " + MAX_FOTOS_EVIDENCIA + " fotos de evidencia.");
        }
        for (MultipartFile archivo : archivos) {
            String tipo = archivo.getContentType();
            if (tipo == null || !tipo.startsWith("image/")) {
                throw new IllegalArgumentException("Las evidencias deben ser imágenes.");
            }
        }

        for (MultipartFile archivo : archivos) {
            Map resultado = cloudinaryIntegration.subir(archivo);
            IncidenciaEvidenciaEntity evidencia = new IncidenciaEvidenciaEntity();
            evidencia.setIncidencia(incidencia);
            evidencia.setUrlFoto((String) resultado.get("secure_url"));
            evidencia.setPublicId((String) resultado.get("public_id"));
            incidencia.getEvidencias().add(evidencia);
        }
        registrarHistorial(incidencia, IncidenciaHistorialEntity.TIPO_EVIDENCIA, null, null,
                archivos.size() == 1 ? "Se adjuntó 1 foto de evidencia" : "Se adjuntaron " + archivos.size() + " fotos de evidencia",
                realizadoPor);

        String nota = comentario == null || comentario.isBlank() ? null : comentario.trim();
        if (nota != null && nota.length() > 500) {
            nota = nota.substring(0, 500);
        }
        aplicarCambioEstado(incidencia, IncidenciaEstados.RESUELTO, nota, realizadoPor);
        return aSeguirIncidenciaDTO(incidenciaRepository.save(incidencia));
    }

    /** Historial de una incidencia: lo ve el ADMIN o el ciudadano que la registró. */
    @Transactional(readOnly = true)
    public List<HistorialResponseDTO> obtenerHistorial(Integer idIncidencia, String nombreUsuario, boolean esAdmin) {
        IncidenciaEntity incidencia = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new EntityNotFoundException("Incidencia no encontrada: " + idIncidencia));
        if (!esAdmin) {
            UsuarioEntity dueno = incidencia.getUsuario();
            if (dueno == null || !dueno.getNombreUsuario().equals(nombreUsuario)) {
                throw new AccessDeniedException("No puedes ver el historial de esta incidencia");
            }
        }
        return historialRepository.findByIncidencia_IdIncidenciaOrderByFechaAscIdHistorialAsc(idIncidencia).stream()
                .map(h -> new HistorialResponseDTO(
                        h.getTipo(),
                        h.getEstadoAnterior(),
                        h.getEstadoNuevo(),
                        h.getDetalle(),
                        esAdmin ? h.getRealizadoPor() : (h.getRealizadoPor() != null ? "Municipalidad" : null),
                        h.getFecha() != null ? h.getFecha().toString() : null))
                .toList();
    }

    /**
     * Cambia el estado dejando constancia en el historial.
     * Para pasar a RESUELTO hace falta al menos una foto de evidencia (opción "Resolver").
     */
    private void aplicarCambioEstado(IncidenciaEntity incidencia, IncidenciaEstados nuevo, String detalle, String realizadoPor) {
        IncidenciaEstados anterior = incidencia.getEstado();
        if (nuevo == anterior) {
            return;
        }
        if (nuevo == IncidenciaEstados.RESUELTO && incidencia.getEvidencias().isEmpty()) {
            throw new IllegalArgumentException("Para marcar la incidencia como resuelta usa la opción \"Resolver\" y adjunta una foto de evidencia.");
        }
        incidencia.setEstado(nuevo);
        incidencia.setFechaResolucion(nuevo == IncidenciaEstados.RESUELTO ? LocalDateTime.now() : null);
        registrarHistorial(incidencia, IncidenciaHistorialEntity.TIPO_ESTADO,
                anterior != null ? anterior.name() : null, nuevo.name(), detalle, realizadoPor);
    }

    private void registrarHistorial(IncidenciaEntity incidencia, String tipo, String estadoAnterior,
                                    String estadoNuevo, String detalle, String realizadoPor) {
        IncidenciaHistorialEntity h = new IncidenciaHistorialEntity();
        h.setIncidencia(incidencia);
        h.setTipo(tipo);
        h.setEstadoAnterior(estadoAnterior);
        h.setEstadoNuevo(estadoNuevo);
        h.setDetalle(detalle);
        h.setRealizadoPor(realizadoPor);
        historialRepository.save(h);
    }

    // Transaccional: el usuario (relacion LAZY) se lee y sus puntos se recalculan
    // dentro de la misma sesion; sin esto falla con "could not initialize proxy - no session"
    @Transactional
    public void eliminarIncidencia(Integer idIncidencia) {
        IncidenciaEntity incidencia = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new EntityNotFoundException("Incidencia no encontrada: " + idIncidencia));
        UsuarioEntity usuario = incidencia.getUsuario();
        incidenciaRepository.delete(incidencia);
        if (usuario != null) {
            sincronizarPuntos(usuario);
        }
    }

    // Los puntos siempre equivalen a las incidencias registradas por el usuario
    public int sincronizarPuntos(UsuarioEntity usuario) {
        int puntos = (int) incidenciaRepository.countByUsuario_NombreUsuario(usuario.getNombreUsuario())
                * PUNTOS_POR_INCIDENCIA;
        if (usuario.getPuntos() == null || usuario.getPuntos() != puntos) {
            usuario.setPuntos(puntos);
            usuarioRepository.save(usuario);
        }
        return puntos;
    }

    private SeguirIncidenciaResponseDTO aSeguirIncidenciaDTO(IncidenciaEntity incidencia) {
        SeguirIncidenciaResponseDTO dto = new SeguirIncidenciaResponseDTO();
        dto.setIdIncidencia(incidencia.getIdIncidencia());
        dto.setDescripcion(incidencia.getDescripcion());
        dto.setEstado(incidencia.getEstado().name());
        dto.setTitulo(incidencia.getTitulo());
        dto.setFecha(incidencia.getFecha() != null ? incidencia.getFecha().toString() : null);
        dto.setCategoria(incidencia.getCategoria());
        dto.setUrlsImagenes(
                incidencia.getFotos().stream()
                        .map(foto -> normalizarUrlCloudinary(foto.getUrlFoto()))
                        .collect(Collectors.toList())
        );
        dto.setDireccionTexto(incidencia.getDireccionTexto());
        CuadrillaEntity cuadrilla = incidencia.getCuadrilla();
        if (cuadrilla != null) {
            dto.setIdCuadrilla(cuadrilla.getIdCuadrilla());
            dto.setCuadrillaNombre(cuadrilla.getNombre());
            dto.setCuadrillaZona(cuadrilla.getZona());
        }
        dto.setFechaAsignacion(incidencia.getFechaAsignacion() != null ? incidencia.getFechaAsignacion().toString() : null);
        dto.setFechaResolucion(incidencia.getFechaResolucion() != null ? incidencia.getFechaResolucion().toString() : null);
        dto.setUrlsEvidencias(
                incidencia.getEvidencias() == null ? List.of() :
                incidencia.getEvidencias().stream()
                        .map(e -> normalizarUrlCloudinary(e.getUrlFoto()))
                        .collect(Collectors.toList())
        );
        UsuarioEntity usuario = incidencia.getUsuario();
        if (usuario != null) {
            dto.setNombreUsuario(usuario.getNombreUsuario());
            String nombre = ((usuario.getNombreCompleto() == null ? "" : usuario.getNombreCompleto()) + " "
                    + (usuario.getApellidoCompleto() == null ? "" : usuario.getApellidoCompleto())).trim();
            dto.setReportadoPor(nombre.isEmpty() ? usuario.getNombreUsuario() : nombre);
        }
        return dto;
    }

    public MetricasResponseDTO obtenerMetricas(String nombreUsuario) {
        long total = incidenciaRepository.countByUsuario_NombreUsuario(nombreUsuario);
        long enProceso = incidenciaRepository.countByUsuario_NombreUsuarioAndEstado(nombreUsuario, IncidenciaEstados.EN_PROCESO);
        long pendientes = incidenciaRepository.countByUsuario_NombreUsuarioAndEstado(nombreUsuario, IncidenciaEstados.PENDIENTE);
        long resueltos = incidenciaRepository.countByUsuario_NombreUsuarioAndEstado(nombreUsuario, IncidenciaEstados.RESUELTO);
        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        long incidenciasEsteMes = incidenciaRepository.countDesdeFecha(nombreUsuario, inicioMes);
        MetricasResponseDTO dto = new MetricasResponseDTO();
        dto.setTotal(total);
        dto.setEnProceso(enProceso);
        dto.setPendientes(pendientes);
        dto.setResueltos(resueltos);
        dto.setIncidenciasEsteMes(incidenciasEsteMes);
        return dto;
    }

    private String normalizarUrlCloudinary(String url) {
        if (url == null) return null;
        if (url.contains("f_auto,q_auto")) return url;
        if (url.contains("/upload/")) {
            return url.replace("/upload/", "/upload/f_auto,q_auto/");
        }
        return url;
    }
}
