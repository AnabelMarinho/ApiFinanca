package ufersa.dev.ApiFinanca.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Componente responsável por criar o banco de dados automaticamente
 * se ele não existir antes da aplicação tentar conectar.
 * 
 * Este listener executa muito cedo no ciclo de vida do Spring Boot,
 * antes da inicialização do DataSource.
 */
public class DatabaseInitializer implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private static boolean initialized = false;

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        // Evita execução múltipla
        if (initialized) {
            return;
        }
        initialized = true;

        try {
            ConfigurableEnvironment environment = event.getEnvironment();
            
            String datasourceUrl = environment.getProperty("spring.datasource.url");
            String username = environment.getProperty("spring.datasource.username");
            String password = environment.getProperty("spring.datasource.password");

            if (datasourceUrl == null || username == null || password == null) {
                logger.warn("Configurações de datasource não encontradas. Pulando inicialização do banco.");
                return;
            }

            // Extrai o nome do banco da URL
            String databaseName = extractDatabaseName(datasourceUrl);
            String baseUrl = extractBaseUrl(datasourceUrl);

            logger.info("Verificando se o banco de dados '{}' existe...", databaseName);

            // Conecta ao banco padrão do PostgreSQL (geralmente 'postgres')
            try (Connection connection = DriverManager.getConnection(baseUrl, username, password);
                 Statement statement = connection.createStatement()) {

                // Verifica se o banco já existe
                String checkDbQuery = String.format(
                    "SELECT 1 FROM pg_database WHERE datname = '%s'",
                    databaseName
                );

                ResultSet resultSet = statement.executeQuery(checkDbQuery);
                boolean databaseExists = resultSet.next();

                if (!databaseExists) {
                    logger.info("Banco de dados '{}' não encontrado. Criando...", databaseName);
                    
                    // Cria o banco de dados
                    // Nota: CREATE DATABASE não pode ser executado dentro de uma transação
                    statement.executeUpdate(String.format("CREATE DATABASE %s", databaseName));
                    
                    logger.info("Banco de dados '{}' criado com sucesso!", databaseName);
                } else {
                    logger.info("Banco de dados '{}' já existe.", databaseName);
                }
            }

        } catch (Exception e) {
            logger.error("Erro ao inicializar o banco de dados: {}", e.getMessage(), e);
            // Não lança exceção para não impedir a inicialização da aplicação
            // O erro será capturado quando a aplicação tentar conectar
        }
    }

    /**
     * Extrai o nome do banco de dados da URL de conexão
     */
    private String extractDatabaseName(String url) {
        // Formato esperado: jdbc:postgresql://localhost:5432/financas_db
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
            String dbName = url.substring(lastSlashIndex + 1);
            // Remove parâmetros de query se existirem
            int paramIndex = dbName.indexOf('?');
            if (paramIndex != -1) {
                dbName = dbName.substring(0, paramIndex);
            }
            return dbName;
        }
        throw new IllegalArgumentException("Não foi possível extrair o nome do banco de dados da URL: " + url);
    }

    /**
     * Extrai a URL base (sem o nome do banco) para conectar ao banco padrão
     */
    private String extractBaseUrl(String url) {
        // Formato esperado: jdbc:postgresql://localhost:5432/financas_db
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            String baseUrl = url.substring(0, lastSlashIndex + 1) + "postgres";
            // Preserva parâmetros de query se existirem
            int paramIndex = url.indexOf('?');
            if (paramIndex != -1) {
                baseUrl += url.substring(paramIndex);
            }
            return baseUrl;
        }
        throw new IllegalArgumentException("Não foi possível extrair a URL base da URL: " + url);
    }
}

