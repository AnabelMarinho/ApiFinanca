# ApiFinanca – Visão Geral Atual

Este documento resume o que já foi implementado e como a aplicação está estruturada até o estágio atual, incluindo o fluxo recente de autenticação com JWT.

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

## 6. Tutorial de Instalação e Execução

### Pré-requisitos

- **Java 17** instalado (`java -version`).
- **Maven** (opcional — o wrapper `mvnw` já está incluso).
- **PostgreSQL** rodando localmente. Crie o banco `financas_db` e ajuste usuário/senha se necessário.

### Passo a passo

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
   - Clique em **Authorize**, selecione `bearer-jwt`, informe `Bearer <seu_token>` e autorize.
   - Execute os demais endpoints diretamente pelo Swagger, conforme as permissões descritas na seção anterior.

5. **Parar a aplicação**  
   Pressione `Ctrl+C` no terminal onde o Spring Boot está rodando.

## 7. Considerações de Segurança

- Substituir `app.jwt.secret` por uma chave de pelo menos 256 bits (ex.: gerar via utilitário) e manter fora do controle de versão em produção.
- Avaliar quais endpoints devem permanecer públicos; hoje alguns `PUT/DELETE` estão liberados apenas para testes.
- Pensar em adicionar roles/perfis caso haja níveis de acesso distintos.

## 8. Próximos Passos Sugeridos

1. **Migrar o banco** para o novo tipo de ID (ou recriar as tabelas) para evitar `ClassCastException`.
2. **Implementar onboarding** para preencher campos adicionais do usuário após o cadastro básico.
3. **Adicionar testes automatizados** para o fluxo de autenticação e regras principais.
4. **Criar migrations (Flyway/Liquibase)** para controlar as mudanças de schema.
5. Revisar políticas CORS/CSRF caso haja frontend separado.

---

Com isso, a aplicação já fornece cadastro, login, emissão de JWT, e documentação via Swagger com suporte ao token, além das camadas de domínio reorganizadas para trabalhar com UUIDs.

