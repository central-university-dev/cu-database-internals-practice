-- ============================================================
-- Практика 1. Исследование физических страниц
-- Необходимо научиться:
--   • видеть реальные tuple на странице,
--   • понимать xmin/xmax/ctid,
--   • связывать логическую строку и физическую версию.
-- ============================================================

-- --------------------------------------------
-- 1. Создание тестовой таблицы
-- --------------------------------------------

CREATE EXTENSION IF NOT EXISTS pageinspect;

DROP TABLE IF EXISTS bank_accounts;

CREATE TABLE bank_accounts (
                               id      serial PRIMARY KEY,
                               owner   text,
                               balance int
);

INSERT INTO bank_accounts(owner, balance)
SELECT 'user_' || g, 1000
FROM generate_series(1, 50) g;

ANALYZE bank_accounts;


-- --------------------------------------------
-- 2. Найти физический файл и размер таблицы
-- --------------------------------------------

SELECT relfilenode
FROM pg_class
WHERE relname = 'bank_accounts';

SELECT pg_relation_size('bank_accounts') / 8192 AS pages;


-- --------------------------------------------
-- 3. Посмотреть содержимое первой страницы
-- --------------------------------------------

SELECT lp, t_xmin, t_xmax, t_ctid, t_infomask, t_infomask2
FROM heap_page_items(get_raw_page('bank_accounts', 0));

-- Вопросы:
--   1) Почему у всех строк примерно одинаковый xmin?
--   2) Почему xmax = 0?
--   3) Что означает ctid? Почему он равен (lp,0)?
--   4) Что такое line pointer и зачем он нужен?
