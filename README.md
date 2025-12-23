# ApiFinanca – Visão Geral Atual

Este documento resume o que já foi implementado e como a aplicação está estruturada até o estágio atual, incluindo o fluxo recente de autenticação com JWT.

## 🚀 Deploy no Railway

Para fazer o deploy desta aplicação no Railway (plataforma gratuita), consulte o guia completo em [DEPLOY_RAILWAY.md](./DEPLOY_RAILWAY.md).

## 📋 Guia de Instalação e Configuração Inicial

Este guia apresenta um passo a passo completo para configurar o ambiente de desenvolvimento e executar o projeto pela primeira vez.

### 1. Instalar IntelliJ IDEA Community Edition

1. Acesse: https://www.jetbrains.com/idea/download/
2. Baixe a versão **Community Edition** (gratuita)
3. Execute o instalador e siga as instruções
4. Complete a instalação e reinicie o computador se solicitado

### 2. Instalar Java 17

1. Baixe o Java 17 (JDK) de uma das seguintes fontes:
   - **Oracle JDK**: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
   - **OpenJDK**: https://adoptium.net/temurin/releases/?version=17
   - **Amazon Corretto**: https://aws.amazon.com/corretto/
2. Execute o instalador e siga as instruções
3. **Configurar variável de ambiente JAVA_HOME** (Windows):
   - Abra as Variáveis de Ambiente do sistema
   - Crie/edite a variável `JAVA_HOME` apontando para a pasta de instalação do JDK (ex: `C:\Program Files\Java\jdk-17`)
   - Adicione `%JAVA_HOME%\bin` ao `PATH`
4. Verifique a instalação abrindo um novo terminal e executando:
   ```bash
   java -version
   ```
   Deve mostrar a versão 17

### 3. Instalar PostgreSQL 16.10 com pgAdmin

1. Acesse: https://www.enterprisedb.com/downloads/postgres-postgresql-downloads
2. Baixe o **PostgreSQL 16.10** para Windows
3. Execute o instalador e siga as instruções:
   - **Importante**: Durante a instalação, anote a senha que você definir para o usuário `postgres` (você precisará dela depois)
   - Certifique-se de que a opção **pgAdmin 4** esteja marcada para instalação
   - Deixe a porta padrão `5432` (ou anote se alterar)
4. Após a instalação, o pgAdmin será aberto automaticamente

### 4. Instalar e Configurar Maven 3.9.11

1. Baixe o Maven:
   - Acesse: https://dlcdn.apache.org/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.tar.gz
   - Ou via link direto: https://dlcdn.apache.org/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.tar.gz
2. **Extraia o arquivo** em uma pasta de sua preferência (ex: `C:\Program Files\Apache\maven`)
3. **Adicionar Maven ao PATH** (Windows):
   - Abra as Variáveis de Ambiente do sistema
   - Edite a variável `PATH` e adicione o caminho para a pasta `bin` do Maven (ex: `C:\Program Files\Apache\maven\apache-maven-3.9.11\bin`)
4. Verifique a instalação abrindo um novo terminal e executando:
   ```bash
   mvn -version
   ```
   Deve mostrar a versão 3.9.11

### 5. Reiniciar o Computador

⚠️ **Importante**: Após configurar as variáveis de ambiente (JAVA_HOME, PATH com Maven), **reinicie o computador** para garantir que todas as alterações sejam aplicadas corretamente.

### 6. Clonar o Projeto no IntelliJ IDEA

1. Abra o **IntelliJ IDEA Community Edition**
2. Na tela inicial, clique em **Get from VCS** (ou **File > New > Project from Version Control**)
3. Cole a URL do repositório Git do projeto
4. Escolha o diretório de destino e clique em **Clone**
5. O IntelliJ detectará automaticamente que é um projeto Maven e começará a importar as dependências
6. Aguarde a sincronização completa do projeto (barra de progresso no canto inferior direito)

### 7. Verificar e Configurar application.properties

1. No IntelliJ, navegue até: `src/main/resources/application.properties`
2. Verifique e ajuste as seguintes configurações conforme sua instalação do PostgreSQL:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/financas_db
   spring.datasource.username=postgres
   spring.datasource.password=12345678
   ```
   - **username**: Geralmente é `postgres` (padrão)
   - **password**: Use a senha que você definiu durante a instalação do PostgreSQL
   - **porta**: Se você alterou a porta padrão (5432), ajuste na URL

### 8. Criar o Banco de Dados no pgAdmin

1. Abra o **pgAdmin 4** (procure no menu Iniciar)
2. Conecte-se ao servidor PostgreSQL (você precisará da senha do usuário `postgres`)
3. Clique com o botão direito em **Databases** > **Create** > **Database...**
4. No campo **Database**, digite: `financas_db`
5. Clique em **Save**
6. O banco de dados será criado e aparecerá na lista

### 9. Executar o Projeto

1. No IntelliJ IDEA, localize a classe principal do Spring Boot (geralmente `ApiFinancaApplication.java`)
2. Clique com o botão direito nela e selecione **Run 'ApiFinancaApplication'**
   - Alternativamente, você pode usar o terminal integrado:
     ```bash
     ./mvnw spring-boot:run
     ```
     (No Windows PowerShell: `.\mvnw.cmd spring-boot:run`)
3. Aguarde a aplicação iniciar. Você verá mensagens no console indicando que o Spring Boot está rodando
4. Quando aparecer algo como `Started ApiFinancaApplication in X.XXX seconds`, a aplicação está pronta

### 10. Acessar o Swagger (Opcional)

1. Abra seu navegador e acesse: **http://localhost:8080/swagger-ui/index.html**
2. Você verá a documentação interativa da API
3. Para testar endpoints protegidos:
   - Primeiro, registre um usuário via `POST /auth/register`
   - Ou faça login via `POST /auth/login`
   - Copie o `token` retornado na resposta
   - Clique no botão **Authorize** no topo da página do Swagger
   - Selecione o esquema `bearer-jwt` e informe: `Bearer <seu_token>`
   - Clique em **Authorize** e depois em **Close**
   - Agora você pode testar os endpoints protegidos diretamente pelo Swagger

---

## 🚀 Execução Rápida (Após Configuração Inicial)

Se você já configurou tudo anteriormente, pode usar este guia rápido:

### Passo a passo rápido

1. **Clonar o repositório**
   ```bash
   git clone https://github.com/<seu-usuario>/ApiFinanca.git
   cd ApiFinanca
   ```

2. **Configurar banco**  
   Certifique-se de que o PostgreSQL está ativo e que o usuário informado em `src/main/resources/application.properties` tem acesso ao banco `financas_db`.

3. **Rodar a aplicação**
   ```bash
   ./mvnw spring-boot:run
   ```
   > No Windows PowerShell: `.\mvnw.cmd spring-boot:run`

4. **Acessar o Swagger**
   - Abra o navegador em: `http://localhost:8080/swagger-ui/index.html`
   - Gere um token via `POST /auth/login` (ou `/auth/register` + `/auth/login`).
   - Clique em **Authorize**, selecione `bearer-jwt`, informe `<seu_token>` ou `Bearer <seu_token>` e autorize.
   - Execute os demais endpoints diretamente pelo Swagger, conforme as permissões descritas na seção anterior.

5. **Parar a aplicação**  
   Pressione `Ctrl+C` no terminal onde o Spring Boot está rodando.

---

## 1. Estrutura do Projeto

- **Stack principal**: Spring Boot 3.5, Java 17, PostgreSQL, Spring Data JPA.
- **Camadas**:
  - `model` para entidades JPA.
  - `repository` com interfaces `JpaRepository`.
  - `service` consolidando regras de negócio.
  - `controller` expondo endpoints REST.
  - `security` cuidando da autenticação/autorização e geração de tokens.
- **DTOs** separadas em `dto` (incluindo pacote `dto.auth`) para isolar contratos de entrada/saída.

## 2. Entidades e Identificadores

- Todas as entidades principais (`Usuario`, `Categoria`, `Transacao`, `TransacaoRecorrente`, `Meta`, `AporteMeta`) usam `UUID` como identificador (`@UuidGenerator`), evitando colisões e facilitando integração com o frontend.
- **Atenção**: o banco precisa que as colunas correspondentes sejam do tipo `uuid`. Se o schema pré-existente ainda estiver com `BIGINT`, é necessário migrar (alterar tipo via SQL/Flyway ou recriar o banco) para que a aplicação suba corretamente.

## 3. Configurações Importantes

- `application.properties`:
  - Define a conexão com o PostgreSQL.
  - Mantém `spring.jpa.hibernate.ddl-auto=update` para desenvolvimento (ideal usar migrações em produção).
  - Configurações JWT:
    - `app.jwt.secret` – chave usada para assinar tokens (substituir por valor forte em produção).
    - `app.jwt.expiration` – validade dos tokens em milissegundos (atual: 1 hora).
- Segurança:
  - `SecurityConfig` ativa segurança stateless, adiciona `JwtAuthenticationFilter` e permite endpoints públicos conforme lista abaixo.
  - `OpenApiConfig` registra o esquema Bearer para exibir o botão **Authorize** no Swagger.

## 4. Fluxo de Autenticação

- **Cadastro (`POST /auth/register`)**:
  - Recebe `nome`, `email`, `senha`.
  - Valida se o email já existe e armazena a senha com BCrypt.
  - Retorna `AuthResponse` com o `token` JWT e dados básicos do usuário.

- **Login (`POST /auth/login`)**:
  - Recebe `email` e `senha`.
  - Autentica via `AuthenticationManager`.
  - Gera novo token JWT e retorna junto com os dados do usuário.

- **Usuário atual (`GET /auth/me`)**:
  - Requer header `Authorization: Bearer <token>`.
  - Retorna os dados do usuário autenticado.

- **Camada de segurança**:
  - `JwtService` gera/valida tokens.
  - `JwtAuthenticationFilter` extrai o token do header, valida e injeta no `SecurityContext`.
  - `CustomUserDetailsService` carrega usuários pelo email.

## 5. Endpoints e Acesso

- **Sem token**:
  - `/auth/**` (cadastro/login).
  - Swagger e OpenAPI (`/swagger-ui/**`, `/v3/api-docs/**`, `/swagger-ui.html`).
  - `GET` em `/usuario/**` (consultas de usuário).
  - `GET` em `/transacao/**`, `/categoria/**`, `/transacao-recorrente/**` (consultas para testes).
  - `PUT`/`DELETE` em `/usuario/**` também estão liberados temporariamente para facilitar ajustes (podem ser protegidos depois).

- **Com token**:
  - `POST /usuario` (criar usuário pela rota administrativa).
  - Mutações de transações, categorias e transações recorrentes (`POST`, `PUT`, `DELETE`).
  - `GET /auth/me`.

- **Swagger**:
  - Abra `http://localhost:8080/swagger-ui/index.html`.
  - Use o botão **Authorize** > esquema `bearer-jwt` e informe `Bearer <token>`.
  - Após autorizar, os endpoints marcados com cadeado serão chamados com JWT automaticamente.

## 6. Considerações de Segurança

- Substituir `app.jwt.secret` por uma chave de pelo menos 256 bits (ex.: gerar via utilitário) e manter fora do controle de versão em produção.
- Avaliar quais endpoints devem permanecer públicos; hoje alguns `PUT/DELETE` estão liberados apenas para testes.
- Pensar em adicionar roles/perfis caso haja níveis de acesso distintos.

## 7. Próximos Passos Sugeridos

1. **Migrar o banco** para o novo tipo de ID (ou recriar as tabelas) para evitar `ClassCastException`.
2. **Implementar onboarding** para preencher campos adicionais do usuário após o cadastro básico.
3. **Adicionar testes automatizados** para o fluxo de autenticação e regras principais.
4. **Criar migrations (Flyway/Liquibase)** para controlar as mudanças de schema.
5. Revisar políticas CORS/CSRF caso haja frontend separado.

---

Com isso, a aplicação já fornece cadastro, login, emissão de JWT, e documentação via Swagger com suporte ao token, além das camadas de domínio reorganizadas para trabalhar com UUIDs.

