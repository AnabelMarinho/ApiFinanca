-- ============================================================================
-- MIGRATION: Criar tabela de alertas persistidos
-- ============================================================================
-- Execute este script no pgAdmin para criar a estrutura necessária
-- para o sistema de alertas persistidos
-- ============================================================================

-- Criar tabela de alertas
CREATE TABLE IF NOT EXISTS alertas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    tipo_alerta VARCHAR(50) NOT NULL,
    mensagem VARCHAR(1000) NOT NULL,
    severidade VARCHAR(20) NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    visto BOOLEAN NOT NULL DEFAULT FALSE,
    data_visto TIMESTAMP,
    meta_id UUID,
    categoria_id UUID,
    referencia_periodo VARCHAR(7),
    
    -- Foreign Keys
    CONSTRAINT fk_alerta_usuario FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_alerta_meta FOREIGN KEY (meta_id) 
        REFERENCES metas(id) ON DELETE CASCADE,
    CONSTRAINT fk_alerta_categoria FOREIGN KEY (categoria_id) 
        REFERENCES categorias(id) ON DELETE SET NULL
);

-- Criar índices para otimizar consultas
CREATE INDEX IF NOT EXISTS idx_alertas_usuario_visto 
    ON alertas(usuario_id, visto);

CREATE INDEX IF NOT EXISTS idx_alertas_usuario_tipo 
    ON alertas(usuario_id, tipo_alerta);

CREATE INDEX IF NOT EXISTS idx_alertas_data_criacao 
    ON alertas(data_criacao DESC);

CREATE INDEX IF NOT EXISTS idx_alertas_referencia_periodo 
    ON alertas(usuario_id, tipo_alerta, referencia_periodo);

-- Comentários para documentação
COMMENT ON TABLE alertas IS 'Alertas persistidos gerados pelo sistema para os usuários';
COMMENT ON COLUMN alertas.usuario_id IS 'Usuário que receberá o alerta';
COMMENT ON COLUMN alertas.tipo_alerta IS 'Tipo do alerta (GASTO_ACIMA_MEDIA, META_PROXIMA_VENCIMENTO, etc)';
COMMENT ON COLUMN alertas.mensagem IS 'Mensagem detalhada do alerta';
COMMENT ON COLUMN alertas.severidade IS 'Nível de severidade (BAIXA, MEDIA, ALTA)';
COMMENT ON COLUMN alertas.data_criacao IS 'Data e hora em que o alerta foi criado';
COMMENT ON COLUMN alertas.visto IS 'Indica se o usuário já visualizou o alerta';
COMMENT ON COLUMN alertas.data_visto IS 'Data e hora em que o alerta foi marcado como visto';
COMMENT ON COLUMN alertas.meta_id IS 'ID da meta relacionada (se aplicável)';
COMMENT ON COLUMN alertas.categoria_id IS 'ID da categoria relacionada (se aplicável)';
COMMENT ON COLUMN alertas.referencia_periodo IS 'Período de referência no formato YYYY-MM para controle de recorrência';

-- ============================================================================
-- VERIFICAÇÃO
-- ============================================================================

-- Verificar se a tabela foi criada
SELECT 
    tablename, 
    schemaname 
FROM pg_tables 
WHERE tablename = 'alertas';

-- Verificar índices criados
SELECT 
    indexname, 
    indexdef 
FROM pg_indexes 
WHERE tablename = 'alertas';

-- Verificar estrutura da tabela
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'alertas'
ORDER BY ordinal_position;

-- ============================================================================
-- DADOS DE TESTE (OPCIONAL)
-- ============================================================================

-- Inserir alguns alertas de teste para o usuário nicolas@gmail.com
-- Descomente e execute se quiser testar

/*
DO $$
DECLARE
    test_usuario_id UUID;
    test_meta_id UUID;
    test_categoria_id UUID;
BEGIN
    -- Buscar ID do usuário de teste
    SELECT id INTO test_usuario_id FROM usuarios WHERE email = 'nicolas@gmail.com';
    
    IF test_usuario_id IS NULL THEN
        RAISE NOTICE 'Usuário nicolas@gmail.com não encontrado';
        RETURN;
    END IF;
    
    -- Buscar IDs de meta e categoria para testes
    SELECT id INTO test_meta_id FROM metas WHERE usuario_id = test_usuario_id LIMIT 1;
    SELECT id INTO test_categoria_id FROM categorias WHERE nome = 'Alimentação' AND user_id IS NULL;
    
    -- Inserir alertas de teste
    INSERT INTO alertas (usuario_id, tipo_alerta, mensagem, severidade, visto, meta_id, categoria_id, referencia_periodo)
    VALUES 
        -- Alerta não visto
        (test_usuario_id, 'GASTO_ACIMA_MEDIA', 
         'Gasto na categoria Alimentação está 82% acima da média histórica', 
         'ALTA', false, NULL, test_categoria_id, NULL),
        
        -- Alerta já visto
        (test_usuario_id, 'META_PROXIMA_VENCIMENTO', 
         'Meta "Viagem de Férias" vence em 5 dias!', 
         'MEDIA', true, test_meta_id, NULL, NULL),
        
        -- Alerta mensal com referência de período
        (test_usuario_id, 'ECONOMIA_POSITIVA', 
         'Parabéns! Você economizou 20% em relação à média', 
         'BAIXA', false, NULL, NULL, TO_CHAR(CURRENT_DATE, 'YYYY-MM'));
    
    RAISE NOTICE 'Alertas de teste criados com sucesso!';
END $$;
*/

-- ============================================================================
-- LIMPEZA (SE NECESSÁRIO)
-- ============================================================================

-- CUIDADO: Isso deleta a tabela e todos os dados!
-- Descomente apenas se precisar recriar do zero

/*
DROP TABLE IF EXISTS alertas CASCADE;
*/

-- ============================================================================
-- CONSULTAS ÚTEIS
-- ============================================================================

-- Ver todos os alertas
-- SELECT * FROM alertas ORDER BY data_criacao DESC;

-- Ver alertas não vistos
-- SELECT * FROM alertas WHERE visto = FALSE ORDER BY data_criacao DESC;

-- Ver alertas de um usuário específico
-- SELECT * FROM alertas WHERE usuario_id = 'SEU_UUID_AQUI' ORDER BY data_criacao DESC;

-- Contar alertas por tipo
-- SELECT tipo_alerta, COUNT(*) FROM alertas GROUP BY tipo_alerta;

-- Contar alertas não vistos por usuário
-- SELECT usuario_id, COUNT(*) FROM alertas WHERE visto = FALSE GROUP BY usuario_id;

