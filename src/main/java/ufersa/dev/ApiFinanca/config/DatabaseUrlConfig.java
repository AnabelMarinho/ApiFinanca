package ufersa.dev.ApiFinanca.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuração para processar a URL do banco de dados do Railway.
 * Adiciona o prefixo 'jdbc:' se necessário e prioriza URL pública sobre privada.
 */
public class DatabaseUrlConfig implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUrlConfig.class);
    private static boolean configured = false;

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        if (configured) {
            return;
        }
        configured = true;

        ConfigurableEnvironment environment = event.getEnvironment();
        Map<String, Object> properties = new HashMap<>();

        // Verifica se já existe SPRING_DATASOURCE_URL configurada
        String springDatasourceUrl = environment.getProperty("SPRING_DATASOURCE_URL");
        if (springDatasourceUrl != null && !springDatasourceUrl.isEmpty()) {
            // Se já tem jdbc:, usa direto
            if (springDatasourceUrl.startsWith("jdbc:")) {
                properties.put("spring.datasource.url", springDatasourceUrl);
                logger.info("Usando SPRING_DATASOURCE_URL: {}", springDatasourceUrl);
            } else {
                // Adiciona prefixo jdbc:
                properties.put("spring.datasource.url", "jdbc:" + springDatasourceUrl);
                logger.info("Adicionando prefixo jdbc: à SPRING_DATASOURCE_URL");
            }
        } else {
            // Tenta usar DATABASE_PUBLIC_URL (URL pública do Railway)
            String publicUrl = environment.getProperty("DATABASE_PUBLIC_URL");
            if (publicUrl != null && !publicUrl.isEmpty()) {
                // Adiciona prefixo jdbc: se necessário
                String jdbcUrl = publicUrl.startsWith("jdbc:") ? publicUrl : "jdbc:" + publicUrl;
                properties.put("spring.datasource.url", jdbcUrl);
                logger.info("Usando DATABASE_PUBLIC_URL (URL pública do Railway)");
            } else {
                // Usa DATABASE_URL como fallback
                String databaseUrl = environment.getProperty("DATABASE_URL");
                if (databaseUrl != null && !databaseUrl.isEmpty()) {
                    String jdbcUrl = databaseUrl.startsWith("jdbc:") ? databaseUrl : "jdbc:" + databaseUrl;
                    properties.put("spring.datasource.url", jdbcUrl);
                    logger.info("Usando DATABASE_URL como fallback");
                }
            }
        }

        if (!properties.isEmpty()) {
            environment.getPropertySources().addFirst(
                new MapPropertySource("databaseUrlConfig", properties)
            );
            logger.info("Configuração de URL do banco de dados aplicada");
        }
    }
}

