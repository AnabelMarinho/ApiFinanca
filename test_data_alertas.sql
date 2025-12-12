-- ============================================================================
-- SCRIPT DE TESTE - SISTEMA DE ALERTAS
-- ============================================================================
-- Este script cria um usuário de teste com dados que ativam todos os alertas
-- Execute no pgAdmin ou qualquer cliente PostgreSQL
-- ============================================================================

-- Limpar dados de teste anteriores (se existirem)
DELETE FROM aporte_metas WHERE meta_id IN (SELECT id FROM metas WHERE usuario_id IN (SELECT id FROM usuarios WHERE email = 'teste.alertas@example.com'));
DELETE FROM metas WHERE usuario_id IN (SELECT id FROM usuarios WHERE email = 'teste.alertas@example.com');
DELETE FROM transacoes WHERE user_id IN (SELECT id FROM usuarios WHERE email = 'teste.alertas@example.com');
DELETE FROM transacoes_recorrentes WHERE user_id IN (SELECT id FROM usuarios WHERE email = 'teste.alertas@example.com');
DELETE FROM preferencias_alerta WHERE usuario_id IN (SELECT id FROM usuarios WHERE email = 'teste.alertas@example.com');
DELETE FROM configuracoes_usuario WHERE usuario_id IN (SELECT id FROM usuarios WHERE email = 'teste.alertas@example.com');
DELETE FROM usuarios WHERE email = 'teste.alertas@example.com';

-- ============================================================================
-- 1. CRIAR USUÁRIO DE TESTE
-- ============================================================================
-- Senha: senha123 (hash BCrypt)
-- Email: teste.alertas@example.com
INSERT INTO usuarios (id, nome, email, senha, faixa_salario, data_criacao, data_atualizacao, data_inicio_controle, primeiro_acesso, saldo_inicial, saldo_atual)
VALUES (
    gen_random_uuid(),
    'Usuário Teste Alertas',
    'teste.alertas@example.com',
    '$2a$10$slYQmyNdGzTn7ZBi.w7TeeWKdvJKhNvfNP6RXS/s.KqPYKr.7BNiq', -- senha123
    5000.00, -- Faixa salarial de R$ 5.000
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    DATE_TRUNC('month', CURRENT_DATE), -- Início do mês atual
    false,
    3000.00, -- Saldo inicial
    2500.00  -- Saldo atual (já gastou um pouco)
);

-- Guardar o ID do usuário para usar nas próximas queries
DO $$
DECLARE
    usuario_id UUID;
    config_id UUID;
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
    -- Buscar ID do usuário criado
    SELECT id INTO usuario_id FROM usuarios WHERE email = 'teste.alertas@example.com';
    
    -- ============================================================================
    -- 2. CRIAR CONFIGURAÇÃO DO USUÁRIO
    -- ============================================================================
    INSERT INTO configuracoes_usuario (id, usuario_id, dia_virada_mes)
    VALUES (gen_random_uuid(), usuario_id, 1)
    RETURNING id INTO config_id;
    
    -- ============================================================================
    -- 3. BUSCAR IDs DAS CATEGORIAS PADRÃO
    -- ============================================================================
    SELECT id INTO cat_alimentacao_id FROM categorias WHERE nome = 'Alimentação' AND user_id IS NULL;
    SELECT id INTO cat_moradia_id FROM categorias WHERE nome = 'Moradia' AND user_id IS NULL;
    SELECT id INTO cat_transporte_id FROM categorias WHERE nome = 'Transporte' AND user_id IS NULL;
    SELECT id INTO cat_lazer_id FROM categorias WHERE nome = 'Lazer' AND user_id IS NULL;
    SELECT id INTO cat_salario_id FROM categorias WHERE nome = 'Salário' AND user_id IS NULL;
    
    -- ============================================================================
    -- 4. CRIAR TRANSAÇÕES HISTÓRICAS (últimos 3 meses)
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
    -- 5. CRIAR TRANSAÇÕES DO MÊS ATUAL
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
    -- 6. CRIAR TRANSAÇÕES RECORRENTES ATIVAS (para projeção)
    -- ============================================================================
    -- Despesas recorrentes altas para ativar PROJECAO_SALDO_NEGATIVO
    INSERT INTO transacoes_recorrentes (id, user_id, categoria_id, tipo, valor, descricao, dia_recorrencia, ativa)
    VALUES 
        (gen_random_uuid(), usuario_id, cat_moradia_id, 'DESPESA', 500.00, 'Conta de luz + água (recorrente)', 15, true),
        (gen_random_uuid(), usuario_id, cat_alimentacao_id, 'DESPESA', 300.00, 'Mercado mensal (recorrente)', 20, true);
    
    -- ============================================================================
    -- 7. CRIAR METAS COM DIFERENTES ESTADOS
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
    
    -- ============================================================================
    -- 8. CRIAR PREFERÊNCIAS DE ALERTAS (TODAS ATIVAS PARA TESTE)
    -- ============================================================================
    INSERT INTO preferencias_alerta (id, usuario_id, tipo_alerta, ativo)
    VALUES 
        (gen_random_uuid(), usuario_id, 'GASTO_ACIMA_MEDIA', true),
        (gen_random_uuid(), usuario_id, 'META_PROXIMA_VENCIMENTO', true),
        (gen_random_uuid(), usuario_id, 'PROJECAO_SALDO_NEGATIVO', true),
        (gen_random_uuid(), usuario_id, 'PROGRESSO_META_LENTO', true),
        (gen_random_uuid(), usuario_id, 'META_ALCANCADA', true),
        (gen_random_uuid(), usuario_id, 'GASTO_CATEGORIA_ELEVADO', true),
        (gen_random_uuid(), usuario_id, 'ECONOMIA_POSITIVA', true);
    
    RAISE NOTICE 'Usuário de teste criado com sucesso!';
    RAISE NOTICE 'Email: teste.alertas@example.com';
    RAISE NOTICE 'Senha: senha123';
    RAISE NOTICE 'ID: %', usuario_id;
    
END $$;

-- ============================================================================
-- VERIFICAÇÕES E CONSULTAS ÚTEIS
-- ============================================================================

-- Ver o usuário criado
SELECT id, nome, email, faixa_salario, saldo_atual, data_inicio_controle, primeiro_acesso
FROM usuarios 
WHERE email = 'teste.alertas@example.com';

-- Ver as metas criadas
SELECT m.nome, m.valor_alvo, m.valor_atual, m.data_alvo, m.data_criacao,
       ROUND((m.valor_atual / m.valor_alvo * 100)::numeric, 2) as percentual_alcancado,
       m.data_alvo - CURRENT_DATE as dias_restantes
FROM metas m
JOIN usuarios u ON m.usuario_id = u.id
WHERE u.email = 'teste.alertas@example.com'
ORDER BY m.data_criacao;

-- Ver transações do mês atual por categoria
SELECT c.nome as categoria, t.tipo, SUM(t.valor) as total
FROM transacoes t
JOIN categorias c ON t.categoria_id = c.id
JOIN usuarios u ON t.user_id = u.id
WHERE u.email = 'teste.alertas@example.com'
  AND t.data >= DATE_TRUNC('month', CURRENT_DATE)
GROUP BY c.nome, t.tipo
ORDER BY t.tipo, total DESC;

-- Ver preferências de alertas
SELECT tipo_alerta, ativo
FROM preferencias_alerta pa
JOIN usuarios u ON pa.usuario_id = u.id
WHERE u.email = 'teste.alertas@example.com'
ORDER BY tipo_alerta;

-- ============================================================================
-- DICAS DE USO
-- ============================================================================
/*
1. Execute todo este script no pgAdmin

2. Faça login na API com:
   Email: teste.alertas@example.com
   Senha: senha123

3. Chame o endpoint GET /api/alertas para ver os alertas ativos

4. Alertas esperados:
   ✅ GASTO_ACIMA_MEDIA - Alimentação está 82% acima da média
   ✅ META_PROXIMA_VENCIMENTO - "Viagem de Férias" vence em 5 dias
   ✅ PROJECAO_SALDO_NEGATIVO - Projeção negativa ao fim do mês
   ✅ PROGRESSO_META_LENTO - "Carro Novo" está atrasada (50% tempo, 20% progresso)
   ✅ META_ALCANCADA - "Notebook Novo" foi alcançada (104%)
   ✅ GASTO_CATEGORIA_ELEVADO - Moradia = 36% da renda (> 30%)
   ❌ ECONOMIA_POSITIVA - Não deve aparecer (gastando mais que o normal)

5. Teste desativar/ativar alertas:
   PUT /api/alertas/preferencias
   {
     "tipo": "GASTO_ACIMA_MEDIA",
     "ativo": false
   }

6. Veja os tipos disponíveis:
   GET /api/alertas/preferencias
*/

