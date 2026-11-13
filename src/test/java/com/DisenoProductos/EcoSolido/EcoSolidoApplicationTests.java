package com.DisenoProductos.EcoSolido;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Levanta la aplicación completa, por eso necesita la base de datos.
 * Solo se ejecuta si existe la variable DB_URL; en GitHub Actions se omite
 * y corren las pruebas unitarias, que no dependen de servicios externos.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class EcoSolidoApplicationTests {

	@Test
	void contextLoads() {
	}

}
