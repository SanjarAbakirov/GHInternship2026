-- ============================================================================
-- Инициализация базы данных PostgreSQL 16 для GHInternship2026
-- ============================================================================
-- При запуске через Docker Compose переменные POSTGRES_DB и POSTGRES_USER
-- автоматически создают выделенную базу и владельца.
-- Данный скрипт гарантирует права на схему public (важно для PostgreSQL 15/16+).
--
-- При ручной настройке PostgreSQL (например, через Homebrew / psql postgres)
-- выполните (подставив надежный пароль):
--   CREATE USER gh_app_user WITH PASSWORD 'YOUR_STRONG_PASSWORD';
--   CREATE DATABASE gh_internship_db OWNER gh_app_user;
--   \c gh_internship_db
--   GRANT ALL ON SCHEMA public TO gh_app_user;

GRANT ALL ON SCHEMA public TO CURRENT_USER;

