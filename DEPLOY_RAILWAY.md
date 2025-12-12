# 🚀 Guia de Deploy no Railway

Este guia explica como fazer o deploy da API ApiFinanca no Railway de forma gratuita.

## 📋 Pré-requisitos

1. Conta no GitHub (se ainda não tiver, crie em https://github.com)
2. Conta no Railway (crie em https://railway.app - pode usar login com GitHub)
3. Repositório Git configurado com o código da aplicação

## 🔧 Passo a Passo para Deploy

### 1. Preparar o Repositório

Certifique-se de que seu código está commitado e enviado para o GitHub:

```bash
git add .
git commit -m "Configuração para deploy no Railway"
git push origin main
```

### 2. Criar Projeto no Railway

1. Acesse https://railway.app e faça login com sua conta GitHub
2. Clique em **"New Project"**
3. Selecione **"Deploy from GitHub repo"**
4. Escolha o repositório `ApiFinanca`
5. O Railway detectará automaticamente que é um projeto Maven/Java

### 3. Adicionar Banco de Dados PostgreSQL

1. No dashboard do projeto Railway, clique em **"+ New"**
2. Selecione **"Database"** > **"Add PostgreSQL"**
3. O Railway criará automaticamente um banco PostgreSQL gratuito
4. **IMPORTANTE**: Anote o nome do serviço do banco (ex: `Postgres`)

### 4. Configurar Variáveis de Ambiente

No dashboard do Railway, vá até o serviço da sua aplicação (não o banco) e clique em **"Variables"**:

#### Variáveis do Banco de Dados

O Railway automaticamente cria variáveis quando você conecta o banco. Você precisa adicionar manualmente:

1. Clique em **"+ New Variable"**
2. Adicione as seguintes variáveis:

| Nome da Variável | Valor | Descrição |
|-----------------|-------|-----------|
| `DATABASE_URL` | `${{Postgres.DATABASE_URL}}` | URL completa do banco (use a referência ao serviço Postgres) |
| `DATABASE_USER` | `${{Postgres.PGUSER}}` | Usuário do banco |
| `DATABASE_PASSWORD` | `${{Postgres.PGPASSWORD}}` | Senha do banco |

**Nota**: Substitua `Postgres` pelo nome exato do seu serviço de banco de dados no Railway.

#### Variáveis de Segurança

Adicione também:

| Nome da Variável | Valor | Descrição |
|-----------------|-------|-----------|
| `JWT_SECRET` | *(gere uma chave segura)* | Chave secreta para assinatura JWT (veja como gerar abaixo) |
| `JWT_EXPIRATION` | `3600000` | Tempo de expiração do token em milissegundos (opcional, padrão: 1 hora) |
| `SHOW_SQL` | `false` | Desabilita logs SQL em produção (recomendado) |

#### Como Gerar JWT_SECRET

Você pode gerar uma chave segura de várias formas:

**Opção 1 - Usando OpenSSL (Linux/Mac):**
```bash
openssl rand -base64 32
```

**Opção 2 - Usando PowerShell (Windows):**
```powershell
-join ((48..57) + (65..90) + (97..122) | Get-Random -Count 32 | ForEach-Object {[char]$_})
```

**Opção 3 - Online:**
Acesse https://www.uuidgenerator.net/api/version4 e gere alguns UUIDs, depois concatene-os.

**Opção 4 - Usando Java:**
```bash
java -cp . -c "System.out.println(java.util.UUID.randomUUID().toString() + java.util.UUID.randomUUID().toString());"
```

### 5. Conectar o Banco de Dados ao Serviço da Aplicação

1. No dashboard do Railway, vá até o serviço da sua aplicação
2. Clique na aba **"Settings"**
3. Role até **"Connect Database"** ou **"Add Service"**
4. Selecione o serviço PostgreSQL que você criou
5. Isso criará automaticamente as variáveis de ambiente `DATABASE_URL`, `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE`

### 6. Configurar o Build

O Railway detecta automaticamente projetos Maven, mas você pode verificar:

1. Vá em **Settings** do serviço da aplicação
2. Verifique se o **Build Command** está como: `mvn clean package -DskipTests`
3. Verifique se o **Start Command** está como: `java -jar target/ApiFinanca-0.0.1-SNAPSHOT.jar`

### 7. Deploy

1. O Railway fará o deploy automaticamente quando você fizer push para o repositório
2. Você também pode clicar em **"Deploy"** manualmente
3. Acompanhe os logs em tempo real na aba **"Deployments"** ou **"Logs"**

### 8. Obter a URL da Aplicação

1. Após o deploy bem-sucedido, vá em **Settings** do serviço
2. Role até **"Networking"** ou **"Domains"**
3. O Railway fornece uma URL pública automaticamente (ex: `https://apifinanca-production.up.railway.app`)
4. Você pode adicionar um domínio customizado se desejar

## 🔍 Verificando se Está Funcionando

1. Acesse a URL fornecida pelo Railway + `/swagger-ui/index.html`
   - Exemplo: `https://seu-app.up.railway.app/swagger-ui/index.html`
2. Você deve ver a documentação Swagger da API
3. Teste o endpoint `POST /auth/register` para criar um usuário

## ⚠️ Troubleshooting

### Erro de Conexão com Banco de Dados

- Verifique se as variáveis `DATABASE_URL`, `DATABASE_USER` e `DATABASE_PASSWORD` estão configuradas
- Certifique-se de que o serviço PostgreSQL está rodando
- Verifique os logs do serviço da aplicação para ver erros específicos

### Erro de Build

- Verifique se o Java 17 está disponível (Railway usa Nixpacks que detecta automaticamente)
- Verifique os logs de build para erros de compilação
- Certifique-se de que o `pom.xml` está correto

### Aplicação não Inicia

- Verifique os logs em tempo real no Railway
- Certifique-se de que a porta está configurada corretamente (Railway usa a variável `PORT` automaticamente)
- Verifique se todas as variáveis de ambiente obrigatórias estão configuradas

### CORS Errors

- Se você tiver um frontend, adicione a URL do Railway nas configurações CORS
- O código já está configurado para aceitar domínios `*.railway.app` e `*.up.railway.app`

## 📝 Notas Importantes

1. **Plano Gratuito**: O Railway oferece um plano gratuito com limites:
   - $5 de crédito grátis por mês
   - Após esgotar, você precisa adicionar método de pagamento (mas pode continuar usando o plano gratuito)
   - O banco PostgreSQL gratuito tem limitações de espaço

2. **Banco de Dados**: O Railway cria o banco automaticamente, mas você pode precisar ajustar o nome do banco na URL se necessário.

3. **Variáveis de Ambiente**: Sempre use variáveis de ambiente para informações sensíveis. Nunca commite secrets no código.

4. **Atualizações**: Toda vez que você fizer push para o branch principal, o Railway fará um novo deploy automaticamente.

5. **Logs**: Os logs estão disponíveis em tempo real no dashboard do Railway. Use-os para debugar problemas.

## 🔄 Após o Deploy

Depois que a aplicação estiver rodando no Railway:

1. **Teste todos os endpoints** via Swagger
2. **Atualize seu frontend** (se tiver) para usar a nova URL da API
3. **Configure CORS** no frontend se necessário
4. **Monitore os logs** periodicamente para garantir que está tudo funcionando

## 📚 Recursos Adicionais

- [Documentação do Railway](https://docs.railway.app)
- [Railway Discord](https://discord.gg/railway) - Para suporte da comunidade
- [Status do Railway](https://status.railway.app) - Para verificar se há problemas na plataforma

---

**Pronto!** Sua API está configurada para rodar no Railway. 🎉

