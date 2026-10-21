package com.DisenoProductos.EcoSolido.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.File;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Backup diario de la base PostgreSQL (Supabase) con pg_dump.
 * Solo se activa con backup.enabled=true (variable BACKUP_ENABLED) y
 * requiere que pg_dump este instalado en la maquina donde corre la app.
 */
@Component
@ConditionalOnProperty(name = "backup.enabled", havingValue = "true")
public class BackupScheduler {
    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.password}")
    private String dbPassword;
    @Value("${spring.datasource.username}")
    private String dbUsername;
    @Value("${pgdump.path:pg_dump}")
    private String pgDumpPath;

    @Scheduled(cron = "0 0 0 * * *")
    public void ejecutarBackup() {
        System.out.println("✅ Scheduler disparado: " + LocalDateTime.now());
        try {
            File dirBackup = new File("../../Database/Backup/");
            if (!dirBackup.exists()) dirBackup.mkdirs();
            System.out.println("Ruta absoluta: " + dirBackup.getAbsolutePath());

            String rutaBackup = "../../Database/Backup/backup_" + LocalDate.now() + ".sql";

            // jdbc:postgresql://host:puerto/bd?params -> host, puerto, bd
            URI uri = URI.create(dbUrl.replaceFirst("^jdbc:", ""));
            String puerto = uri.getPort() > 0 ? String.valueOf(uri.getPort()) : "5432";
            String baseDatos = uri.getPath().replaceFirst("^/", "");

            ProcessBuilder pb = new ProcessBuilder(
                    pgDumpPath,
                    "-h", uri.getHost(),
                    "-p", puerto,
                    "-U", dbUsername,
                    "-d", baseDatos,
                    "--schema=public",
                    "--no-owner",
                    "--no-privileges",
                    "-f", rutaBackup
            );
            pb.environment().put("PGPASSWORD", dbPassword);
            pb.environment().put("PGSSLMODE", "require");
            pb.redirectErrorStream(true);
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("✅ Backup generado: " + rutaBackup);
            } else {
                System.err.println("❌ pg_dump falló con código: " + exitCode);
            }

        } catch (Exception e) {
            System.err.println("Error al generar backup: " + e.getMessage());
        }
    }
}
