package com.DisenoProductos.EcoSolido.Services;

import com.DisenoProductos.EcoSolido.Integrations.RENIECIntegration;
import com.DisenoProductos.EcoSolido.Models.DTOs.*;
import com.DisenoProductos.EcoSolido.Models.Entities.UsuarioEntity;
import com.DisenoProductos.EcoSolido.Models.States.Rol;
import com.DisenoProductos.EcoSolido.Repositories.IncidenciaRepository;
import com.DisenoProductos.EcoSolido.Repositories.UsuarioRepository;
import com.DisenoProductos.EcoSolido.Security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {
    @Autowired
    private final UsuarioRepository usuarioRepository;
    @Autowired
    private final RENIECIntegration reniecIntegration;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private IncidenciaRepository incidenciaRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, RENIECIntegration reniecIntegration) {
        this.usuarioRepository = usuarioRepository;
        this.reniecIntegration = reniecIntegration;
    }

    public CrearUsuarioResponseDTO registrarUsuario(CrearUsuarioRequestDTO usuarioRequestDTO) throws Exception {
        if (usuarioRepository.findByNombreUsuario(usuarioRequestDTO.getNombreUsuario()).isPresent()) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }
        if (usuarioRepository.existsByCorreoElectronico(usuarioRequestDTO.getCorreoElectronico())) {
            throw new RuntimeException("El correo electrónico ya está registrado");
        }
        if (usuarioRepository.existsByTelefono(usuarioRequestDTO.getTelefono())) {
            throw new RuntimeException("El teléfono ya está registrado");
        }
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNombreCompleto(usuarioRequestDTO.getNombreCompleto());
        usuario.setApellidoCompleto(usuarioRequestDTO.getApellidoCompleto());
        usuario.setDni(usuarioRequestDTO.getDni());
        usuario.setNombreUsuario(usuarioRequestDTO.getNombreUsuario());
        usuario.setContrasena(passwordEncoder.encode(usuarioRequestDTO.getContrasena()));
        usuario.setTelefono(usuarioRequestDTO.getTelefono());
        usuario.setCorreoElectronico(usuarioRequestDTO.getCorreoElectronico());
        usuario.setPreguntaSeguridad(usuarioRequestDTO.getPreguntaSeguridad());
        usuario.setRespuestaPregunta(usuarioRequestDTO.getRespuestaPregunta().trim().toLowerCase());
        usuario.setRol(Rol.CIUDADANO);
        if (!reniecIntegration.validarDni(usuario.getDni(),
                usuario.getNombreCompleto(),
                usuario.getApellidoCompleto())) {
            throw new RuntimeException("El DNI no coincide con los nombres y apellidos registrados en RENIEC");
        }
        UsuarioEntity guardado = usuarioRepository.save(usuario);
        return aCrearUsuarioResponse(guardado);
    }

    private LoginRequestDTO loginRequest(String nombreUsuario,String contrasena){
        LoginRequestDTO requestDTO=new LoginRequestDTO();
        requestDTO.setNombreUsuario(nombreUsuario);
        requestDTO.setContrasena(contrasena);
        return requestDTO;
    }

    public LoginResponseDTO autenticarUsuario(String nombreUsuario, String contrasena){
        LoginRequestDTO requestDTO=loginRequest(nombreUsuario, contrasena);
        UsuarioEntity usuario = usuarioRepository
                .findByNombreUsuario(requestDTO.getNombreUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos"));
        if (!passwordEncoder.matches(requestDTO.getContrasena(), usuario.getContrasena())) {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }
        String nuevoToken = jwtUtil.generarToken(usuario.getNombreUsuario(),
                usuario.getRol() != null ? usuario.getRol().name() : "CIUDADANO");
        return new LoginResponseDTO(nuevoToken, usuario.getNombreUsuario(),usuario.getRol(),usuario.getPuntos());
    }

    public List<UsuarioListarDTO> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::aUsuarioListarDTO)
                .collect(Collectors.toList());
    }

    private CrearUsuarioResponseDTO aCrearUsuarioResponse(UsuarioEntity usuario) {
        CrearUsuarioResponseDTO dto = new CrearUsuarioResponseDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setNombreCompleto(usuario.getNombreCompleto());
        dto.setApellidoCompleto(usuario.getApellidoCompleto());
        dto.setNombreUsuario(usuario.getNombreUsuario());
        dto.setCorreoElectronico(usuario.getCorreoElectronico());
        dto.setRol(usuario.getRol());
        dto.setPuntos(usuario.getPuntos());
        return dto;
    }

    private UsuarioListarDTO aUsuarioListarDTO(UsuarioEntity usuario) {
        UsuarioListarDTO dto = new UsuarioListarDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setNombreCompleto(usuario.getNombreCompleto());
        dto.setApellidoCompleto(usuario.getApellidoCompleto());
        dto.setDni(usuario.getDni());
        dto.setTelefono(usuario.getTelefono());
        dto.setCorreoElectronico(usuario.getCorreoElectronico());
        dto.setNombreUsuario(usuario.getNombreUsuario());
        dto.setPuntos(usuario.getPuntos());
        dto.setRol(usuario.getRol());
        dto.setTotalIncidencias(incidenciaRepository.countByUsuario_NombreUsuario(usuario.getNombreUsuario()));
        return dto;
    }

    public VerificarCorreoTelResponseDTO verificarCorreoOTelefono(VerificarCorreoTelRequestDTO requestDTO) {
        boolean existe;
        if (requestDTO.getCorreo() != null && !requestDTO.getCorreo().isEmpty()) {
            existe = usuarioRepository.existsByCorreoElectronico(requestDTO.getCorreo());
        } else {
            existe = usuarioRepository.existsByTelefono(requestDTO.getTelefono());
        }
        VerificarCorreoTelResponseDTO response = new VerificarCorreoTelResponseDTO();
        response.setEstaRegistrado(existe);
        return response;
    }
    public String obtenerPreguntaSeguridad(VerificarCorreoTelRequestDTO requestDTO) {
        Optional<UsuarioEntity> usuario;
        if (requestDTO.getCorreo() != null && !requestDTO.getCorreo().isEmpty()) {
            usuario = usuarioRepository.findByCorreoElectronico(requestDTO.getCorreo());
        } else {
            usuario = usuarioRepository.findByTelefono(requestDTO.getTelefono());
        }
        return usuario
                .map(UsuarioEntity::getPreguntaSeguridad)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
    public Optional<UsuarioEntity> obtenerUsuarioPorNombreUsuario(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario);
    }

    public RespuestaSeguridadResponseDTO verificarRespuesta(RespuestaSeguridadRequestDTO requestDTO){
        Optional<UsuarioEntity> usuarioOpt;
        if (requestDTO.getCorreo() != null && !requestDTO.getCorreo().isEmpty()) {
            usuarioOpt = usuarioRepository.findByCorreoElectronico(requestDTO.getCorreo());
        } else {
            usuarioOpt = usuarioRepository.findByTelefono(requestDTO.getTelefono());
        }
        UsuarioEntity usuario = usuarioOpt
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        boolean coinciden = respuestaCorrecta(usuario, requestDTO.getRespuesta());
        RespuestaSeguridadResponseDTO response = new RespuestaSeguridadResponseDTO();
        response.setCoinciden(coinciden);
        return response;
    }
    public RestablecerContrasenaResponseDTO restablecerContrasena(RestablecerContrasenaRequestDTO requestDTO){
        RestablecerContrasenaResponseDTO response = new RestablecerContrasenaResponseDTO();
        if (!Objects.equals(requestDTO.getNuevaContrasena(), requestDTO.getNuevaContrasenaRepetida())) {
            response.setExito(false);
            throw new RuntimeException("Las contraseñas no coinciden");
        }
        Optional<UsuarioEntity> usuarioOpt;
        if (requestDTO.getCorreo() != null && !requestDTO.getCorreo().isEmpty()) {
            usuarioOpt = usuarioRepository.findByCorreoElectronico(requestDTO.getCorreo());
        } else {
            usuarioOpt = usuarioRepository.findByTelefono(requestDTO.getTelefono());
        }
        UsuarioEntity usuario = usuarioOpt
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!respuestaCorrecta(usuario, requestDTO.getRespuesta())) {
            throw new RuntimeException("La respuesta de seguridad es incorrecta");
        }
        if (requestDTO.getNuevaContrasena() == null || requestDTO.getNuevaContrasena().length() < 6) {
            throw new RuntimeException("La contraseña debe tener al menos 6 caracteres");
        }
        usuario.setContrasena(passwordEncoder.encode(requestDTO.getNuevaContrasena()));
        usuarioRepository.save(usuario);
        response.setExito(true);
        return response;
    }

    private boolean respuestaCorrecta(UsuarioEntity usuario, String respuesta) {
        return respuesta != null
                && usuario.getRespuestaPregunta() != null
                && usuario.getRespuestaPregunta().trim().equalsIgnoreCase(respuesta.trim());
    }
}
