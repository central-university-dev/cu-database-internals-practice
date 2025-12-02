-- ============================================================
-- Практика 4. Блокировки, deadlocks, индексные записи
-- Необходимо:
--   • увидеть реальный deadlock,
--   • понять механизм блокировок UPDATE,
--   • научиться читать pg_locks,
--   • посмотреть физическую страницу индекса.
-- ============================================================


-- --------------------------------------------
-- 1. Подготовка данных
-- --------------------------------------------

DROP INDEX IF EXISTS idx_balance;
CREATE INDEX idx_balance ON bank_accounts(balance);
ANALYZE bank_accounts;


-- --------------------------------------------
-- 2. Создание deadlock
-- --------------------------------------------

-- SESSION A:
BEGIN;
UPDATE bank_accounts SET balance = balance + 10 WHERE id = 10;

-- SESSION B:
BEGIN;
UPDATE bank_accounts SET balance = balance + 20 WHERE id = 11;

-- SESSION A:
UPDATE bank_accounts SET balance = balance + 10 WHERE id = 11;  -- ждём B

-- SESSION B:
UPDATE bank_accounts SET balance = balance + 20 WHERE id = 10;  -- deadlock


COMMIT;
-- --------------------------------------------
-- 3. SUPERUSER: диагностика блокировок
-- --------------------------------------------

SELECT pid, locktype, mode, granted, relation, page, tuple
FROM pg_locks
ORDER BY pid;

SELECT pid, state, query
FROM pg_stat_activity
WHERE query NOT ILIKE '%pg_locks%';


-- Вопросы:
--   1) какие ресурсы блокировали A и B?
--   2) как формируется цикл ожидания?
--   3) почему именно один процесс выбран жертвой?
--   4) какие типы блокировок использует UPDATE?


-- --------------------------------------------
-- 4. Мини-исследование индекса (B-tree)
-- --------------------------------------------

-- Смотрим первую страницу индекса:
SELECT * FROM bt_page_items('idx_balance', 1);

-- Вопросы:
--   • как соотносятся ключи в индексе и строки в таблице?
--   • почему index tuple не "исчезают" сразу после DELETE/UPDATE?
