# ⚠️ Configuração Temporária do Banco de Dados

## Status Atual

As credenciais do banco de dados do Railway estão **temporariamente** configuradas diretamente no arquivo `application.properties`.

**⚠️ ATENÇÃO**: Esta é uma configuração temporária. Você deve mover essas credenciais para variáveis de ambiente no Railway o mais rápido possível por questões de segurança.

## Credenciais Configuradas

- **URL**: `jdbc:postgresql://interchange.proxy.rlwy.net:43797/railway`
- **Usuário**: `postgres`
- **Senha**: `tzsThDaphtefbXZCUoNmqCpwBWwTYgDy`
- **Banco**: `railway`

## Como Migrar para Variáveis de Ambiente (Recomendado)

### No Railway:

1. Vá até o serviço da sua aplicação no Railway
2. Clique em **"Variables"**
3. Adicione as seguintes variáveis:

```
DATABASE_URL=jdbc:postgresql://interchange.proxy.rlwy.net:43797/railway
DATABASE_USER=postgres
DATABASE_PASSWORD=tzsThDaphtefbXZCUoNmqCpwBWwTYgDy
```

4. Depois, atualize o `application.properties` para usar apenas variáveis de ambiente:

```properties
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/financas_db}
spring.datasource.username=${DATABASE_USER:postgres}
spring.datasource.password=${DATABASE_PASSWORD:12345678}
```

### Para Desenvolvimento Local:

Se você quiser usar um banco local para desenvolvimento, você pode:

1. Criar um arquivo `application-local.properties` com as configurações locais
2. Ou usar variáveis de ambiente locais que sobrescrevem os valores padrão

## Segurança

- ✅ As credenciais estão funcionando para deploy no Railway
- ⚠️ **NÃO** commite essas credenciais em repositórios públicos
- ✅ O arquivo `application.properties` já está no `.gitignore` (mas seria melhor usar variáveis de ambiente)

