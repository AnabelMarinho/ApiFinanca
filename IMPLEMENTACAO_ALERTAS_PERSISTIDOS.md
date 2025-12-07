# Implementação de Alertas Persistidos

## 📋 O Que Foi Implementado

### 1. **Modelo de Dados**
- ✅ **Entidade `Alerta`** criada com:
  - id, usuário, tipoAlerta, mensagem, severidade
  - dataCriacao, visto, dataVisto
  - metaId, categoriaId, referenciaPeriodo (para controle de recorrência)
  - Índices para otimizar consultas

### 2. **Repository**
- ✅ **AlertaRepository** com métodos para:
  - Buscar alertas não vistos
  - Buscar todos os alertas
  - Verificar alertas recentes (por tipo, meta, categoria, período)
  - Contar não vistos
  - Deletar antigos

### 3. **Services**
- ✅ **AlertaGerenciamentoService** para:
  - Listar alertas (não vistos / todos)
  - Marcar como visto
  - Deletar alertas
  - Criar alertas com regras de recorrência
  - Limpar alertas antigos (>90 dias)

### 4. **Scheduler**
- ✅ **AlertaScheduler** com jobs agendados:
  - **Diário 8h**: Verificar metas próximas do vencimento (7 dias e 1 dia)
  - **Semanal (Domingo 9h)**: Verificar gastos acima da média
  - **Diário 10h**: Verificar alertas mensais (dia virada) + progresso de metas
  - **Mensal**: Limpar alertas antigos

### 5. **Controller**
- ✅ **Novos endpoints** criados:
  - `GET /api/alertas` - Lista alertas não vistos
  - `GET /api/alertas/todos` - Lista todos (vistos + não vistos)
  - `PUT /api/alertas/{id}/marcar-visto` - Marca como visto
  - `DELETE /api/alertas/{id}` - Deleta alerta
  - `GET /api/alertas/contador-nao-vistos` - Conta não vistos
  - `GET /api/alertas/preferencias` - Lista tipos disponíveis (já existia)
  - `PUT /api/alertas/preferencias` - Atualiza preferências (já existia)

### 6. **DTOs**
- ✅ **AlertaResponse** atualizado com:
  - id, dataCriacao, visto, dataVisto
  - Método `fromEntity()` para converter entidade

## 🔧 Regras de Recorrência Implementadas

| Tipo de Alerta | Frequência | Observações |
|----------------|-----------|-------------|
| **GASTO_ACIMA_MEDIA** | 1x por semana | Máximo 1 alerta a cada 7 dias |
| **META_PROXIMA_VENCIMENTO** | 2x por meta | Quando falta 7 dias + 1 dia antes |
| **PROJECAO_SALDO_NEGATIVO** | 1x por mês | No dia de virada do usuário |
| **PROGRESSO_META_LENTO** | 1x por meta a cada 7 dias | Verifica em períodos específicos |
| **META_ALCANCADA** | 1x única vez | Só quando meta é alcançada |
| **GASTO_CATEGORIA_ELEVADO** | 1x a cada 3 dias por categoria | Ao criar transação |
| **ECONOMIA_POSITIVA** | 1x por mês | No dia de virada do usuário |

## ⚠️ O Que Ainda Precisa Ser Implementado

### 1. **Lógica Completa dos Métodos de Detecção no Scheduler**
Atualmente os métodos estão como `TODO`. Precisa implementar:
- `verificarGastoAcimaMediaParaUsuario()`
- `verificarProjecaoSaldoNegativoParaUsuario()`
- `verificarEconomiaPositivaParaUsuario()`
- `verificarProgressoMetasParaUsuario()`

**Solução**: Copiar a lógica dos métodos do `AlertaService` e adaptar para chamar `alertaGerenciamentoService.criarAlertaSeNecessario()`

### 2. **Hook para Criar Alerta ao Criar Transação**
Para `GASTO_CATEGORIA_ELEVADO`, precisa:
- Adicionar lógica no `TransacaoService.salvar()` ou `criar()`
- Após salvar transação, verificar se gastou > 30% da renda
- Chamar `alertaGerenciamentoService.criarAlertaSeNecessario()`

### 3. **Hook para Criar Alerta ao Alcançar Meta**
Para `META_ALCANCADA`, precisa:
- Adicionar lógica quando aporte é adicionado e meta é alcançada
- Verificar em `AporteMetaService` ou onde aportes são criados
- Chamar `alertaGerenciamentoService.criarAlertaSeNecessario()`

### 4. **Migration do Banco de Dados**
Criar a tabela `alertas`:

```sql
CREATE TABLE alertas (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    tipo_alerta VARCHAR(50) NOT NULL,
    mensagem VARCHAR(1000) NOT NULL,
    severidade VARCHAR(20) NOT NULL,
    data_criacao TIMESTAMP NOT NULL,
    visto BOOLEAN NOT NULL DEFAULT FALSE,
    data_visto TIMESTAMP,
    meta_id UUID,
    categoria_id UUID,
    referencia_periodo VARCHAR(7),
    CONSTRAINT fk_alerta_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_alerta_meta FOREIGN KEY (meta_id) REFERENCES metas(id),
    CONSTRAINT fk_alerta_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id)
);

CREATE INDEX idx_alertas_usuario_visto ON alertas(usuario_id, visto);
CREATE INDEX idx_alertas_usuario_tipo ON alertas(usuario_id, tipo_alerta);
CREATE INDEX idx_alertas_data_criacao ON alertas(data_criacao);
```

### 5. **Testes**
- Testar schedulers (pode usar `@Scheduled` com cron expressions de teste)
- Testar regras de recorrência
- Testar criação automática de alertas

## 📊 Fluxo Completo

```
1. Scheduler roda periodicamente
   ↓
2. Verifica condições para todos os usuários
   ↓
3. AlertaGerenciamentoService verifica:
   - Usuário tem preferência ativa?
   - Pode criar alerta (regra de recorrência)?
   ↓
4. Se SIM, cria Alerta no banco
   ↓
5. Usuário acessa app e vê alertas não vistos
   ↓
6. Usuário marca como visto
   ↓
7. Alerta fica marcado, não aparece mais em "não vistos"
```

## 🎯 Próximos Passos

1. **Criar a migration SQL** e executar no banco
2. **Implementar os métodos TODO** no AlertaScheduler
3. **Adicionar hooks** em TransacaoService e no gerenciamento de metas
4. **Compilar e testar**
5. **Ajustar horários dos schedulers** conforme necessidade
6. **Documentar** os novos endpoints no Swagger

## 🔄 Migração Gradual

Você pode manter ambos os sistemas funcionando:
- **Antigo** (`AlertaService.obterAlertasAtivos()`): Cálculo em tempo real
- **Novo** (`AlertaGerenciamentoService`): Alertas persistidos

Depois migrar completamente para o novo sistema quando tudo estiver testado.

## 💡 Melhorias Futuras

- [ ] Notificações push quando alertas são criados
- [ ] Email com resumo semanal de alertas
- [ ] Dashboard com estatísticas de alertas
- [ ] Permitir usuário configurar horário de recebimento
- [ ] Snooze de alertas (adiar para depois)
- [ ] Categorizar alertas por prioridade

