-- Script SQL para criar o banco de dados financas_db
-- Execute este script no PostgreSQL se a criação automática não funcionar

-- Conecte-se ao banco 'postgres' (banco padrão) antes de executar este script
-- No psql: \c postgres
-- No pgAdmin: Selecione o banco 'postgres' antes de executar

-- Verifica se o banco já existe e cria se não existir
SELECT 'CREATE DATABASE financas_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'financas_db')\gexec

-- Alternativa (se o comando acima não funcionar):
-- CREATE DATABASE financas_db;

