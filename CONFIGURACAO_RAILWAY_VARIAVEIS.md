# ✅ Configuração de Variáveis no Railway

## Variáveis que o Railway Cria Automaticamente

Quando você conecta o banco PostgreSQL ao serviço da aplicação, o Railway cria automaticamente:

- `SPRING_DATASOURCE_URL` - URL completa do banco
- `SPRING_DATASOURCE_USERNAME` - Usuário do banco
- `SPRING_DATASOURCE_PASSWORD` - Senha do banco
- `SPRING_JPA_HIBERNATE_DDL_AUTO` - Configuração do Hibernate (opcional)

## ✅ O que já está configurado

O `application.properties` já está configurado para usar essas variáveis automaticamente. O Spring Boot mapeia variáveis com prefixo `SPRING_` automaticamente para propriedades do Spring.

## 📝 Variáveis Adicionais que Você Pode Configurar

No Railway, na seção **Variables** do seu serviço, você pode adicionar:

### Obrigatórias (Recomendado):

1. **JWT_SECRET**
   - **Valor**: Gere uma chave segura (veja como gerar abaixo)
   - **Descrição**: Chave secreta para assinar tokens JWT
   - **Exemplo**: `a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0`

### Opcionais:

2. **SHOW_SQL**
   - **Valor**: `false` (recomendado para produção)
   - **Descrição**: Desabilita logs SQL no console
   - **Padrão**: `false` (já configurado)

3. **JWT_EXPIRATION**
   - **Valor**: `3600000` (1 hora em milissegundos)
   - **Descrição**: Tempo de expiração do token JWT
   - **Padrão**: `3600000` (já configurado)

## 🔑 Como Gerar JWT_SECRET

### Opção 1 - PowerShell (Windows):
```powershell
-join ((48..57) + (65..90) + (97..122) | Get-Random -Count 64 | ForEach-Object {[char]$_})
```

### Opção 2 - Online:
Acesse https://www.uuidgenerator.net/api/version4 e gere 4 UUIDs, depois concatene-os.

### Opção 3 - OpenSSL (se tiver instalado):
```bash
openssl rand -base64 64
```

### Opção 4 - Java:
```bash
java -cp . -c "System.out.println(java.util.UUID.randomUUID().toString().replace(\"-\", \"\") + java.util.UUID.randomUUID().toString().replace(\"-\", \"\"));"
```

## ✅ Resumo

**Você já configurou:**
- ✅ Variáveis do banco de dados (Railway criou automaticamente)

**Você ainda precisa configurar (recomendado):**
- ⚠️ `JWT_SECRET` - Gere uma chave segura e adicione no Railway

**Opcional:**
- `SHOW_SQL` = `false` (já está como padrão)
- `JWT_EXPIRATION` = `3600000` (já está como padrão)

## 🚀 Próximo Passo

1. Gere o `JWT_SECRET` usando um dos métodos acima
2. No Railway, vá em **Variables** do serviço da aplicação
3. Clique em **"+ New Variable"**
4. Adicione:
   - **Nome**: `JWT_SECRET`
   - **Valor**: (cole a chave que você gerou)
5. Salve e faça o deploy novamente

Pronto! Sua aplicação está configurada! 🎉

