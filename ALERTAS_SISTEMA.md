# Sistema de Gerenciamento de Alertas

## Visão Geral

Sistema que permite aos usuários gerenciar suas preferências de recebimento de diferentes tipos de alertas financeiros. Os alertas são presets que não podem ser criados pelo usuário, mas podem ser ativados ou desativados conforme sua preferência.

## Tipos de Alertas Disponíveis

### Alertas Ativados por Padrão ✅

1. **GASTO_ACIMA_MEDIA**
   - **Nome**: "Gasto acima da média"
   - **Descrição**: Notifica quando o gasto em uma categoria está significativamente acima da média histórica
   - **Critério**: Gasto atual 20% ou mais acima da média dos últimos 3 meses
   - **Severidade**: Média (20-50% acima) ou Alta (>50% acima)

2. **META_PROXIMA_VENCIMENTO**
   - **Nome**: "Meta próxima do vencimento"
   - **Descrição**: Notifica quando uma meta está próxima da data alvo (7 dias ou menos)
   - **Critério**: Faltam 7 dias ou menos para a data alvo da meta
   - **Severidade**: Média (4-7 dias) ou Alta (0-3 dias)

3. **PROJECAO_SALDO_NEGATIVO**
   - **Nome**: "Projeção de saldo negativo"
   - **Descrição**: Notifica quando a projeção indica saldo negativo ao final do mês
   - **Critério**: Projeção baseada em receitas/despesas realizadas + recorrentes esperadas
   - **Severidade**: Média ou Alta (baseado na magnitude do saldo negativo)

4. **PROGRESSO_META_LENTO**
   - **Nome**: "Progresso de meta abaixo do esperado"
   - **Descrição**: Notifica quando o progresso de uma meta está abaixo do esperado em relação ao tempo decorrido
   - **Critério**: Verifica se na metade (ou mais) do tempo, o progresso está 20% ou mais atrás do esperado
   - **Exemplo**: Se 60% do tempo passou mas apenas 30% do valor foi alcançado
   - **Severidade**: Média (20-40% atrás) ou Alta (>40% atrás)

5. **META_ALCANCADA**
   - **Nome**: "Meta alcançada"
   - **Descrição**: Notifica quando uma meta foi alcançada com sucesso
   - **Critério**: Valor atual ≥ valor alvo
   - **Severidade**: Baixa (mensagem positiva)

### Alertas Desativados por Padrão ❌

6. **GASTO_CATEGORIA_ELEVADO**
   - **Nome**: "Gasto elevado em categoria"
   - **Descrição**: Notifica quando o gasto em uma categoria excede um percentual significativo da renda
   - **Critério**: Gasto em uma categoria > 30% da faixa salarial
   - **Severidade**: Média (30-50%) ou Alta (>50%)
   - **Requisito**: Usuário deve ter faixa salarial cadastrada

7. **ECONOMIA_POSITIVA**
   - **Nome**: "Economia acima do esperado"
   - **Descrição**: Notifica quando você economizou mais do que o esperado no período
   - **Critério**: Despesas atuais 15% ou mais abaixo da média dos últimos 3 meses
   - **Severidade**: Baixa (mensagem positiva)

## Endpoints da API

### 1. Obter Alertas Ativos
```
GET /api/alertas
```
**Descrição**: Retorna todos os alertas ativos para o usuário autenticado, baseado em suas preferências.

**Headers**:
- `Authorization: Bearer {token}`

**Query Parameters**:
- `usuarioId` (opcional): UUID do usuário

**Response**: Lista de `AlertaResponse`
```json
[
  {
    "tipo": "GASTO_ACIMA_MEDIA",
    "mensagem": "Gasto na categoria 'Alimentação' está 35% acima da média histórica (R$ 1.350,00 vs média de R$ 1.000,00)",
    "severidade": "MEDIA"
  }
]
```

### 2. Listar Tipos de Alertas Disponíveis
```
GET /api/alertas/preferencias
```
**Descrição**: Retorna todos os tipos de alertas disponíveis com suas configurações e preferências do usuário.

**Headers**:
- `Authorization: Bearer {token}`

**Response**: Lista de `TipoAlertaInfoResponse`
```json
[
  {
    "tipo": "GASTO_ACIMA_MEDIA",
    "nome": "Gasto acima da média",
    "descricao": "Notifica quando o gasto em uma categoria está significativamente acima da média histórica",
    "ativo": true,
    "ativoPorPadrao": true
  },
  {
    "tipo": "ECONOMIA_POSITIVA",
    "nome": "Economia acima do esperado",
    "descricao": "Notifica quando você economizou mais do que o esperado no período",
    "ativo": false,
    "ativoPorPadrao": false
  }
]
```

### 3. Atualizar Preferência de Alerta
```
PUT /api/alertas/preferencias
```
**Descrição**: Permite ao usuário ativar ou desativar um tipo específico de alerta.

**Headers**:
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Request Body**: `AtualizarPreferenciaRequest`
```json
{
  "tipo": "ECONOMIA_POSITIVA",
  "ativo": true
}
```

**Response**: 
- Status: `204 No Content` (sucesso)
- Status: `400 Bad Request` (validação falhou)
- Status: `404 Not Found` (usuário não encontrado)

## Modelos de Dados

### PreferenciaAlerta (Entidade)
```java
@Entity
@Table(name = "preferencias_alerta")
public class PreferenciaAlerta {
    private UUID id;
    private Usuario usuario;
    private TipoAlerta tipoAlerta;
    private Boolean ativo;
}
```

**Constraint**: Unique(usuario_id, tipo_alerta) - Um usuário só pode ter uma preferência por tipo de alerta.

### TipoAlerta (Enum)
```java
public enum TipoAlerta {
    GASTO_ACIMA_MEDIA("...", "...", true),
    // ... outros tipos
    
    private String nome;
    private String descricao;
    private boolean ativoPorPadrao;
}
```

## Fluxo de Inicialização

1. **Novo Usuário**: Quando o usuário completa o onboarding, todas as preferências são criadas automaticamente com os valores padrão.

2. **Usuário Existente**: Se um usuário não possui preferências cadastradas, o sistema usa automaticamente os valores padrão definidos no enum `TipoAlerta`.

3. **Adição de Novos Tipos**: Se novos tipos de alertas forem adicionados ao sistema:
   - Usuários novos receberão automaticamente no onboarding
   - Usuários existentes receberão o padrão definido no enum quando acessarem alertas
   - Ao atualizar qualquer preferência, a nova será criada explicitamente

## Exemplos de Uso

### Ativar alerta de economia positiva
```bash
curl -X PUT https://api.exemplo.com/api/alertas/preferencias \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "ECONOMIA_POSITIVA",
    "ativo": true
  }'
```

### Desativar alerta de gasto acima da média
```bash
curl -X PUT https://api.exemplo.com/api/alertas/preferencias \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "GASTO_ACIMA_MEDIA",
    "ativo": false
  }'
```

### Listar todas as preferências
```bash
curl -X GET https://api.exemplo.com/api/alertas/preferencias \
  -H "Authorization: Bearer {token}"
```

### Obter alertas ativos
```bash
curl -X GET https://api.exemplo.com/api/alertas \
  -H "Authorization: Bearer {token}"
```

## Considerações Técnicas

1. **Performance**: As preferências são carregadas apenas quando necessário (lazy loading).

2. **Transações**: Todas as operações de criação/atualização são transacionais para garantir consistência.

3. **Validação**: Os tipos de alertas são validados através do enum, garantindo que apenas tipos válidos sejam aceitos.

4. **Migração**: Usuários antigos que não possuem preferências cadastradas continuarão funcionando normalmente com os valores padrão.

5. **Extensibilidade**: Novos tipos de alertas podem ser adicionados facilmente ao enum `TipoAlerta`.

## Melhorias Futuras (Sugestões)

- [ ] Adicionar alertas personalizáveis (limites definidos pelo usuário)
- [ ] Notificações push/email para alertas de alta severidade
- [ ] Histórico de alertas visualizados/não visualizados
- [ ] Dashboard com estatísticas de alertas
- [ ] Configuração de frequência de alertas (diário, semanal, mensal)
- [ ] Alertas baseados em Machine Learning (padrões de gasto anormais)

