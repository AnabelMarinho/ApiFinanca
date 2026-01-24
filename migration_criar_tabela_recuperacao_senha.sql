-- ============================================================================
-- MIGRATION: Criar tabela de recuperação de senha
-- ============================================================================
-- Execute este script no pgAdmin para criar a estrutura necessária
-- para o sistema de recuperação de senha
-- ============================================================================

-- Criar tabela de recuperação de senha
CREATE TABLE IF NOT EXISTS recuperacao_senha (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    codigo VARCHAR(5) NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_expiracao TIMESTAMP NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT FALSE
);

-- Criar índice para melhorar performance nas buscas
CREATE INDEX IF NOT EXISTS idx_recuperacao_senha_email_codigo 
    ON recuperacao_senha(email, codigo) 
    WHERE usado = FALSE;

-- Criar índice para limpeza de registros expirados
CREATE INDEX IF NOT EXISTS idx_recuperacao_senha_data_expiracao 
    ON recuperacao_senha(data_expiracao);

-- ============================================================================
-- FIM DA MIGRATION
-- ============================================================================
