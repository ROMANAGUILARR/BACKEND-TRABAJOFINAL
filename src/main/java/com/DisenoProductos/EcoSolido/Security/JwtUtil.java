package com.DisenoProductos.EcoSolido.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    @Value("${token.secret.value}")
    private String SECRET_KEY;

    public String generarToken(String nombreUsuario, String rol){
        return Jwts.builder()
                .setSubject(nombreUsuario)
                .claim("rol", rol != null ? rol : "CIUDADANO")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 8)) // 8 horas
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes())
                .compact();
    }

    public String extraerNombreUsuario(String token) {
        return parseClaims(token).getSubject();
    }

    public String extraerRol(String token) {
        Object rol = parseClaims(token).get("rol");
        return rol != null ? rol.toString() : "CIUDADANO";
    }

    public boolean validarToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }
}
