-- ============================================================================
-- SCRIPT DE TESTE - ADICIONAR DADOS DE ALERTAS NO USUÁRIO EXISTENTE
-- ============================================================================
-- Este script adiciona transações e metas ao usuário para testar alertas
-- UUID do usuário: 94e8b475-8962-4408-b3f6-09001fbf4773
-- Dia de virada: 4 (hoje)
-- ============================================================================

-- Limpar dados anteriores deste usuário (se houver)
DELETE FROM aporte_metas WHERE meta_id IN (SELECT id FROM metas WHERE usuario_id = '94e8b475-8962-4408-b3f6-09001fbf4773');
DELETE FROM metas WHERE usuario_id = '94e8b475-8962-4408-b3f6-09001fbf4773';
DELETE FROM transacoes WHERE user_id = '94e8b475-8962-4408-b3f6-09001fbf4773';
DELETE FROM transacoes_recorrentes WHERE user_id = '94e8b475-8962-4408-b3f6-09001fbf4773';
DELETE FROM alertas WHERE usuario_id = '94e8b475-8962-4408-b3f6-09001fbf4773';

-- ============================================================================
-- ATUALIZAR DADOS DO USUÁRIO
-- ============================================================================
UPDATE usuarios 
SET 
    faixa_salario = 5000.00,
    saldo_inicial = 3000.00,
    saldo_atual = 2500.00,
    primeiro_acesso = false,
    data_inicio_controle = DATE_TRUNC('month', CURRENT_DATE)
WHERE id = '94e8b475-8962-4408-b3f6-09001fbf4773';

-- ============================================================================
-- GARANTIR CONFIGURAÇÃO DO USUÁRIO (DIA DE VIRADA = 4)
-- ============================================================================
INSERT INTO configuracoes_usuario (id, usuario_id, dia_virada_mes)
VALUES (gen_random_uuid(), '94e8b475-8962-4408-b3f6-09001fbf4773', 4)
ON CONFLICT (usuario_id) DO UPDATE SET dia_virada_mes = 4;

-- ============================================================================
-- CRIAR PREFERÊNCIAS DE ALERTAS (TODAS ATIVAS)
-- ============================================================================
-- Limpar preferências antigas
DELETE FROM preferencias_alerta WHERE usuario_id = '94e8b475-8962-4408-b3f6-09001fbf4773';

-- Inserir todas as preferências ativas
INSERT INTO preferencias_alerta (id, usuario_id, tipo_alerta, ativo)
VALUES 
    (gen_random_uuid(), '94e8b475-8962-4408-b3f6-09001fbf4773', 'GASTO_ACIMA_MEDIA', true),
    (gen_random_uuid(), '94e8b475-8962-4408-b3f6-09001fbf4773', 'META_PROXIMA_VENCIMENTO', true),
    (gen_random_uuid(), '94e8b475-8962-4408-b3f6-09001fbf4773', 'PROJECAO_SALDO_NEGATIVO', true),
    (gen_random_uuid(), '94e8b475-8962-4408-b3f6-09001fbf4773', 'PROGRESSO_META_LENTO', true),
    (gen_random_uuid(), '94e8b475-8962-4408-b3f6-09001fbf4773', 'META_ALCANCADA', true),
    (gen_random_uuid(), '94e8b475-8962-4408-b3f6-09001fbf4773', 'GASTO_CATEGORIA_ELEVADO', true),
    (gen_random_uuid(), '94e8b475-8962-4408-b3f6-09001fbf4773', 'ECONOMIA_POSITIVA', true);

-- ============================================================================
-- ADICIONAR TRANSAÇÕES E METAS
-- ============================================================================
DO $$
DECLARE
    usuario_id UUID := '94e8b475-8962-4408-b3f6-09001fbf4773';
    cat_alimentacao_id UUID;
    cat_moradia_id UUID;
    cat_transporte_id UUID;
    cat_lazer_id UUID;
    cat_salario_id UUID;
    meta1_id UUID;
    meta2_id UUID;
    meta3_id UUID;
    meta4_id UUID;
BEGIN
    -- ============================================================================
    -- BUSCAR IDs DAS CATEGORIAS PADRÃO
    -- ============================================================================
    SELECT id INTO cat_alimentacao_id FROM categorias WHERE nome = 'Alimentação' AND user_id IS NULL;
    SELECT id INTO cat_moradia_id FROM categorias WHERE nome = 'Moradia' AND user_id IS NULL;
    SELECT id INTO cat_transporte_id FROM categorias WHERE nome = 'Transporte' AND user_id IS NULL;
    SELECT id INTO cat_lazer_id FROM categorias WHERE nome = 'Lazer' AND user_id IS NULL;
    SELECT id INTO cat_salario_id FROM categorias WHERE nome = 'Salário' AND user_id IS NULL;
    
    -- ============================================================================
    -- CRIAR TRANSAÇÕES HISTÓRICAS (últimos 3 meses)
    -- ============================================================================
    -- Para gerar GASTO_ACIMA_MEDIA: histórico estável + gasto atual elevado
    
    -- MÊS -3 (3 meses atrás) - Gastos normais
    INSERT INTO transacoes (id, user_id, categoria_id, tipo, valor, descricao, data)
    VALUES 
        (gen_random_uuid(), usuario_id, cat_alimentacao_id, 'DESPESA', 800.00, 'Supermercado', CURRENT_DATE - INTERVAL '90 days'),
        (gen_random_uuid(), usuario_id, cat_moradia_id, 'DESPESA', 1200.00, 'Aluguel', CURRENT_DATE - INTERVAL '90 days'),
        (gen_random_uuid(), usuario_id, cat_transporte_id, 'DESPESA', 300.00, 'Combustível', CURRENT_DATE - INTERVAL '90 days'),
        (gen_random_uuid(), usuario_id, cat_lazer_id, 'DESPESA', 200.00, 'Cinema', CURRENT_DATE - INTERVAL '90 days'),
        (gen_random_uuid(), usuario_id, cat_salario_id, 'RECEITA', 5000.00, 'Salário', CURRENT_DATE - INTERVAL '90 days');
    
    -- MÊS -2 (2 meses atrás) - Gastos normais
    INSERT INTO transacoes (id, user_id, categoria_id, tipo, valor, descricao, data)
    VALUES 
        (gen_random_uuid(), usuario_id, cat_alimentacao_id, 'DESPESA', 850.00, 'Supermercado', CURRENT_DATE - INTERVAL '60 days'),
        (gen_random_uuid(), usuario_id, cat_moradia_id, 'DESPESA', 1200.00, 'Aluguel', CURRENT_DATE - INTERVAL '60 days'),
        (gen_random_uuid(), usuario_id, cat_transporte_id, 'DESPESA', 280.00, 'Combustível', CURRENT_DATE - INTERVAL '60 days'),
        (gen_random_uuid(), usuario_id, cat_lazer_id, 'DESPESA', 150.00, 'Restaurante', CURRENT_DATE - INTERVAL '60 days'),
        (gen_random_uuid(), usuario_id, cat_salario_id, 'RECEITA', 5000.00, 'Salário', CURRENT_DATE - INTERVAL '60 days');
    
    -- MÊS -1 (1 mês atrás) - Gastos normais
    INSERT INTO transacoes (id, user_id, categoria_id, tipo, valor, descricao, data)
    VALUES 
        (gen_random_uuid(), usuario_id, cat_alimentacao_id, 'DESPESA', 820.00, 'Supermercado', CURRENT_DATE - INTERVAL '30 days'),
        (gen_random_uuid(), usuario_id, cat_moradia_id, 'DESPESA', 1200.00, 'Aluguel', CURRENT_DATE - INTERVAL '30 days'),
        (gen_random_uuid(), usuario_id, cat_transporte_id, 'DESPESA', 320.00, 'Combustível', CURRENT_DATE - INTERVAL '30 days'),
        (gen_random_uuid(), usuario_id, cat_lazer_id, 'DESPESA', 180.00, 'Show', CURRENT_DATE - INTERVAL '30 days'),
        (gen_random_uuid(), usuario_id, cat_salario_id, 'RECEITA', 5000.00, 'Salário', CURRENT_DATE - INTERVAL '30 days');
    
    -- ============================================================================
    -- CRIAR TRANSAÇÕES DO MÊS ATUAL
    -- ============================================================================
    -- Para ativar GASTO_ACIMA_MEDIA: Alimentação muito acima da média (~823 -> 1500)
    -- Para ativar GASTO_CATEGORIA_ELEVADO: Moradia > 30% da renda (1800 > 1500)
    -- Para ativar PROJECAO_SALDO_NEGATIVO: Muitas despesas, poucas receitas
    
    INSERT INTO transacoes (id, user_id, categoria_id, tipo, valor, descricao, data)
    VALUES 
        -- RECEITAS BAIXAS (só R$ 2.000 dos R$ 5.000 esperados)
        (gen_random_uuid(), usuario_id, cat_salario_id, 'RECEITA', 2000.00, 'Adiantamento salarial', CURRENT_DATE - INTERVAL '5 days'),
        
        -- DESPESAS ELEVADAS
        (gen_random_uuid(), usuario_id, cat_alimentacao_id, 'DESPESA', 1500.00, 'Supermercado + Delivery', CURRENT_DATE - INTERVAL '10 days'), -- > 20% acima da média!
        (gen_random_uuid(), usuario_id, cat_moradia_id, 'DESPESA', 1800.00, 'Aluguel + Condomínio', CURRENT_DATE - INTERVAL '8 days'), -- > 30% da renda!
        (gen_random_uuid(), usuario_id, cat_transporte_id, 'DESPESA', 400.00, 'Combustível + Uber', CURRENT_DATE - INTERVAL '7 days'),
        (gen_random_uuid(), usuario_id, cat_lazer_id, 'DESPESA', 250.00, 'Viagem fim de semana', CURRENT_DATE - INTERVAL '3 days');
    
    -- ============================================================================
    -- CRIAR TRANSAÇÕES RECORRENTES ATIVAS (para projeção)
    -- ============================================================================
    -- Despesas recorrentes altas para ativar PROJECAO_SALDO_NEGATIVO
    INSERT INTO transacoes_recorrentes (id, user_id, categoria_id, tipo, valor, descricao, dia_recorrencia, ativa)
    VALUES 
        (gen_random_uuid(), usuario_id, cat_moradia_id, 'DESPESA', 500.00, 'Conta de luz + água (recorrente)', 15, true),
        (gen_random_uuid(), usuario_id, cat_alimentacao_id, 'DESPESA', 300.00, 'Mercado mensal (recorrente)', 20, true);
    
    -- ============================================================================
    -- CRIAR METAS COM DIFERENTES ESTADOS
    -- ============================================================================
    
    -- META 1: Próxima do vencimento (vence em 5 dias) - Ativa META_PROXIMA_VENCIMENTO
    INSERT INTO metas (id, nome, valor_alvo, valor_atual, data_alvo, data_criacao, usuario_id)
    VALUES (
        gen_random_uuid(),
        'Viagem de Férias',
        3000.00,
        1800.00, -- Faltam R$ 1.200
        CURRENT_DATE + INTERVAL '5 days', -- Vence em 5 dias!
        CURRENT_TIMESTAMP - INTERVAL '30 days',
        usuario_id
    )
    RETURNING id INTO meta1_id;
    
    -- Criar alguns aportes para a meta 1
    INSERT INTO aporte_metas (id, meta_id, valor, data)
    VALUES 
        (gen_random_uuid(), meta1_id, 800.00, CURRENT_TIMESTAMP - INTERVAL '25 days'),
        (gen_random_uuid(), meta1_id, 500.00, CURRENT_TIMESTAMP - INTERVAL '15 days'),
        (gen_random_uuid(), meta1_id, 500.00, CURRENT_TIMESTAMP - INTERVAL '5 days');
    
    -- META 2: Progresso lento - Ativa PROGRESSO_META_LENTO
    -- Criada há 60 dias, vence em 60 dias (50% do tempo passou)
    -- Mas só alcançou 20% do valor (deveria estar em 50%)
    INSERT INTO metas (id, nome, valor_alvo, valor_atual, data_alvo, data_criacao, usuario_id)
    VALUES (
        gen_random_uuid(),
        'Carro Novo',
        10000.00,
        2000.00, -- Apenas 20% alcançado
        CURRENT_DATE + INTERVAL '60 days', -- Mais 60 dias
        CURRENT_TIMESTAMP - INTERVAL '60 days', -- Criada há 60 dias (50% do tempo passou!)
        usuario_id
    )
    RETURNING id INTO meta2_id;
    
    INSERT INTO aporte_metas (id, meta_id, valor, data)
    VALUES 
        (gen_random_uuid(), meta2_id, 1000.00, CURRENT_TIMESTAMP - INTERVAL '50 days'),
        (gen_random_uuid(), meta2_id, 500.00, CURRENT_TIMESTAMP - INTERVAL '30 days'),
        (gen_random_uuid(), meta2_id, 500.00, CURRENT_TIMESTAMP - INTERVAL '10 days');
    
    -- META 3: Meta alcançada - Ativa META_ALCANCADA
    INSERT INTO metas (id, nome, valor_alvo, valor_atual, data_alvo, data_criacao, usuario_id)
    VALUES (
        gen_random_uuid(),
        'Notebook Novo',
        2500.00,
        2600.00, -- Alcançou 104%!
        CURRENT_DATE + INTERVAL '30 days',
        CURRENT_TIMESTAMP - INTERVAL '45 days',
        usuario_id
    )
    RETURNING id INTO meta3_id;
    
    INSERT INTO aporte_metas (id, meta_id, valor, data)
    VALUES 
        (gen_random_uuid(), meta3_id, 1000.00, CURRENT_TIMESTAMP - INTERVAL '40 days'),
        (gen_random_uuid(), meta3_id, 800.00, CURRENT_TIMESTAMP - INTERVAL '25 days'),
        (gen_random_uuid(), meta3_id, 800.00, CURRENT_TIMESTAMP - INTERVAL '10 days');
    
    -- META 4: Meta futura normal (não deve gerar alertas)
    INSERT INTO metas (id, nome, valor_alvo, valor_atual, data_alvo, data_criacao, usuario_id)
    VALUES (
        gen_random_uuid(),
        'Casa Própria',
        100000.00,
        15000.00, -- 15% alcançado
        CURRENT_DATE + INTERVAL '730 days', -- 2 anos
        CURRENT_TIMESTAMP - INTERVAL '90 days', -- Criada há 3 meses
        usuario_id
    )
    RETURNING id INTO meta4_id;
    
    INSERT INTO aporte_metas (id, meta_id, valor, data)
    VALUES 
        (gen_random_uuid(), meta4_id, 5000.00, CURRENT_TIMESTAMP - INTERVAL '80 days'),
        (gen_random_uuid(), meta4_id, 5000.00, CURRENT_TIMESTAMP - INTERVAL '50 days'),
        (gen_random_uuid(), meta4_id, 5000.00, CURRENT_TIMESTAMP - INTERVAL '20 days');
    
    RAISE NOTICE 'Dados de teste adicionados com sucesso!';
    RAISE NOTICE 'Usuário ID: %', usuario_id;
    RAISE NOTICE 'Dia de virada configurado: 4 (hoje)';
    
END $$;

-- ============================================================================
-- VERIFICAÇÕES
-- ============================================================================

-- Ver o usuário atualizado
SELECT id, nome, email, faixa_salario, saldo_atual, data_inicio_controle, primeiro_acesso
FROM usuarios 
WHERE id = '94e8b475-8962-4408-b3f6-09001fbf4773';

-- Ver configuração do usuário
SELECT usuario_id, dia_virada_mes
FROM configuracoes_usuario
WHERE usuario_id = '94e8b475-8962-4408-b3f6-09001fbf4773';

-- Ver as metas criadas
SELECT m.nome, m.valor_alvo, m.valor_atual, m.data_alvo, m.data_criacao,
       ROUND((m.valor_atual / m.valor_alvo * 100)::numeric, 2) as percentual_alcancado,
       m.data_alvo - CURRENT_DATE as dias_restantes
FROM metas m
WHERE m.usuario_id = '94e8b475-8962-4408-b3f6-09001fbf4773'
ORDER BY m.data_criacao;

-- Ver transações do mês atual por categoria
SELECT c.nome as categoria, t.tipo, SUM(t.valor) as total
FROM transacoes t
JOIN categorias c ON t.categoria_id = c.id
WHERE t.user_id = '94e8b475-8962-4408-b3f6-09001fbf4773'
  AND t.data >= DATE_TRUNC('month', CURRENT_DATE)
GROUP BY c.nome, t.tipo
ORDER BY t.tipo, total DESC;

-- Ver preferências de alertas
SELECT tipo_alerta, ativo
FROM preferencias_alerta
WHERE usuario_id = '94e8b475-8962-4408-b3f6-09001fbf4773'
ORDER BY tipo_alerta;

-- Contar totais
SELECT 
    (SELECT COUNT(*) FROM transacoes WHERE user_id = '94e8b475-8962-4408-b3f6-09001fbf4773') as total_transacoes,
    (SELECT COUNT(*) FROM metas WHERE usuario_id = '94e8b475-8962-4408-b3f6-09001fbf4773') as total_metas,
    (SELECT COUNT(*) FROM preferencias_alerta WHERE usuario_id = '94e8b475-8962-4408-b3f6-09001fbf4773') as total_preferencias,
    (SELECT COUNT(*) FROM alertas WHERE usuario_id = '94e8b475-8962-4408-b3f6-09001fbf4773') as total_alertas;

-- Ver alertas criados (se houver)
SELECT tipo_alerta, mensagem, severidade, visto, data_criacao
FROM alertas
WHERE usuario_id = '94e8b475-8962-4408-b3f6-09001fbf4773'
ORDER BY data_criacao DESC;

-- ============================================================================
-- CRIAR ALERTAS DE TESTE MANUALMENTE (PARA VER IMEDIATAMENTE)
-- ============================================================================
-- Como os schedulers só rodam em horários específicos, vamos criar alguns
-- alertas manualmente para você poder testar os endpoints agora mesmo!

DO $$
DECLARE
    v_usuario_id UUID := '94e8b475-8962-4408-b3f6-09001fbf4773';
    meta_viagem_id UUID;
    meta_carro_id UUID;
    meta_notebook_id UUID;
    cat_alimentacao_id UUID;
    cat_moradia_id UUID;
BEGIN
    -- Buscar IDs das metas criadas
    SELECT id INTO meta_viagem_id FROM metas WHERE usuario_id = v_usuario_id AND nome = 'Viagem de Férias';
    SELECT id INTO meta_carro_id FROM metas WHERE usuario_id = v_usuario_id AND nome = 'Carro Novo';
    SELECT id INTO meta_notebook_id FROM metas WHERE usuario_id = v_usuario_id AND nome = 'Notebook Novo';
    
    -- Buscar IDs das categorias
    SELECT id INTO cat_alimentacao_id FROM categorias WHERE nome = 'Alimentação' AND user_id IS NULL;
    SELECT id INTO cat_moradia_id FROM categorias WHERE nome = 'Moradia' AND user_id IS NULL;
    
    -- Criar alertas de teste
    INSERT INTO alertas (id, usuario_id, tipo_alerta, mensagem, severidade, data_criacao, visto, meta_id, categoria_id, referencia_periodo)
    VALUES 
        -- 1. GASTO_ACIMA_MEDIA
        (gen_random_uuid(), v_usuario_id, 'GASTO_ACIMA_MEDIA', 
         'Gasto na categoria ''Alimentação'' está 82% acima da média histórica (R$ 1.500,00 vs média de R$ 823,33)', 
         'ALTA', CURRENT_TIMESTAMP, false, NULL, cat_alimentacao_id, NULL),
        
        -- 2. META_PROXIMA_VENCIMENTO
        (gen_random_uuid(), v_usuario_id, 'META_PROXIMA_VENCIMENTO', 
         'Meta ''Viagem de Férias'' vence em 5 dia(s)! Faltam R$ 1.200,00 para alcançar o valor alvo de R$ 3.000,00', 
         'MEDIA', CURRENT_TIMESTAMP, false, meta_viagem_id, NULL, NULL),
        
        -- 3. PROJECAO_SALDO_NEGATIVO
        (gen_random_uuid(), v_usuario_id, 'PROJECAO_SALDO_NEGATIVO', 
         'Projeção indica saldo negativo de R$ 450,00 ao final do mês. Receitas esperadas: R$ 2.000,00, Despesas esperadas: R$ 4.950,00', 
         'ALTA', CURRENT_TIMESTAMP, false, NULL, NULL, TO_CHAR(CURRENT_DATE, 'YYYY-MM')),
        
        -- 4. PROGRESSO_META_LENTO
        (gen_random_uuid(), v_usuario_id, 'PROGRESSO_META_LENTO', 
         'Meta ''Carro Novo'': Progresso abaixo do esperado. Decorreram 50% do tempo, mas apenas 20% foi alcançado. Esperado: R$ 5.000,00, Atual: R$ 2.000,00', 
         'ALTA', CURRENT_TIMESTAMP, false, meta_carro_id, NULL, NULL),
        
        -- 5. META_ALCANCADA (já visto para exemplo)
        (gen_random_uuid(), v_usuario_id, 'META_ALCANCADA', 
         '🎉 Parabéns! Meta ''Notebook Novo'' alcançada! Você conquistou 104% do objetivo (R$ 2.600,00 de R$ 2.500,00)', 
         'BAIXA', CURRENT_TIMESTAMP - INTERVAL '1 day', true, meta_notebook_id, NULL, NULL),
        
        -- 6. GASTO_CATEGORIA_ELEVADO
        (gen_random_uuid(), v_usuario_id, 'GASTO_CATEGORIA_ELEVADO', 
         'Gasto na categoria ''Moradia'' está alto em relação à sua renda (36%). Valor: R$ 1.800,00', 
         'MEDIA', CURRENT_TIMESTAMP, false, NULL, cat_moradia_id, NULL);
    
    RAISE NOTICE '✅ 6 alertas de teste criados! (5 não vistos + 1 visto)';
    RAISE NOTICE '📱 Acesse GET /api/alertas para ver os alertas não vistos';
    RAISE NOTICE '📋 Acesse GET /api/alertas/todos para ver todos os alertas';
    
END $$;

-- ============================================================================
-- INSTRUÇÕES DE USO
-- ============================================================================
/*
1. Execute este script no pgAdmin

2. Os dados foram criados para ativar os schedulers automaticamente.
   Como hoje é dia 4 (dia de virada), alguns alertas devem ser criados automaticamente.

3. Teste os novos endpoints de alertas:
   - GET /api/alertas (ver alertas não vistos)
   - GET /api/alertas/todos (ver todos os alertas)
   - GET /api/alertas/contador-nao-vistos (quantos não vistos)
   - PUT /api/alertas/{id}/marcar-visto (marcar como visto)
   - DELETE /api/alertas/{id} (deletar alerta)
   
4. Endpoints de preferências:
   - GET /api/alertas/preferencias (ver tipos disponíveis)
   - PUT /api/alertas/preferencias (ativar/desativar tipos)

5. Alertas que devem ser criados pelos schedulers:
   ✅ GASTO_ACIMA_MEDIA - Domingo às 9h
   ✅ META_PROXIMA_VENCIMENTO - Diariamente às 8h (quando faltar 7 dias e 1 dia)
   ✅ PROJECAO_SALDO_NEGATIVO - Hoje às 10h (dia de virada)
   ✅ PROGRESSO_META_LENTO - Diariamente às 10h
   ✅ META_ALCANCADA - Quando meta for alcançada (hook)
   ✅ GASTO_CATEGORIA_ELEVADO - Ao criar transação (hook)
   ✅ ECONOMIA_POSITIVA - Hoje às 10h (dia de virada)

6. Para testar imediatamente SEM esperar os schedulers:
   Você pode criar alertas manualmente executando:
   
   INSERT INTO alertas (id, usuario_id, tipo_alerta, mensagem, severidade, visto)
   VALUES 
       (gen_random_uuid(), '94e8b475-8962-4408-b3f6-09001fbf4773', 'GASTO_ACIMA_MEDIA', 
        'Gasto na categoria Alimentação está 82% acima da média histórica', 'ALTA', false),
       (gen_random_uuid(), '94e8b475-8962-4408-b3f6-09001fbf4773', 'META_PROXIMA_VENCIMENTO', 
        'Meta "Viagem de Férias" vence em 5 dias!', 'MEDIA', false);

7. Para limpar os dados de teste futuramente:
   DELETE FROM alertas WHERE usuario_id = '94e8b475-8962-4408-b3f6-09001fbf4773';
   DELETE FROM aporte_metas WHERE meta_id IN (SELECT id FROM metas WHERE usuario_id = '94e8b475-8962-4408-b3f6-09001fbf4773');
   DELETE FROM metas WHERE usuario_id = '94e8b475-8962-4408-b3f6-09001fbf4773';
   DELETE FROM transacoes WHERE user_id = '94e8b475-8962-4408-b3f6-09001fbf4773';
   DELETE FROM transacoes_recorrentes WHERE user_id = '94e8b475-8962-4408-b3f6-09001fbf4773';
*/

