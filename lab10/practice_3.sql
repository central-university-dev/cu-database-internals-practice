-- ============================================================
-- Практика 3. Snapshot, non-repeatable read, цепочки версий
-- Необходимо:
--   • понять различие RC и RR через реальные эксперименты,
--   • увидеть множественные версии одной строки на странице,
--   • уметь читать цепочку версий через ctid.
-- ============================================================


-- =====================================================================
-- 1. Сценарий non-repeatable read (уровень изоляции: Read Committed)
-- =====================================================================

-- SESSION A:
BEGIN;
SELECT balance FROM bank_accounts WHERE id = 2;

-- Пояснение:
--   SESSION A читает текущую видимую версию (по своему snapshot),
--   но под Read Committed snapshot обновляется ПЕРЕД КАЖДЫМ оператором SELECT.
--   Поэтому последующий SELECT может увидеть новую версию строки.


-- SESSION B:
BEGIN;
UPDATE bank_accounts SET balance = balance + 500 WHERE id = 2;
COMMIT;

-- SESSION A: выполняет повторный SELECT
SELECT balance FROM bank_accounts WHERE id = 2;
COMMIT;

-- Вопросы:
--   1) Почему первый SELECT и второй SELECT в одной транзакции дают разные значения?
--   2) Что означает «каждый SELECT под RC берёт новый snapshot»?
--   3) Какая версия строки видна во втором SELECT?
--   4) Что происходило физически: возникла ли новая версия в heap?


-- =====================================================================
-- 2. Сценарий snapshot isolation (Repeatable Read)
-- =====================================================================

-- SESSION A:
BEGIN TRANSACTION ISOLATION LEVEL REPEATABLE READ;
SELECT balance FROM bank_accounts WHERE id = 3;

-- Пояснение:
--   В Repeatable Read snapshot фиксируется ОДИН РАЗ при начале транзакции.
--   Все последующие SELECT будут использовать тот же snapshot,
--   даже если снаружи происходят UPDATE/COMMIT.

-- SESSION B:
BEGIN;
UPDATE bank_accounts SET balance = balance + 500 WHERE id = 3;
COMMIT;

-- SESSION A: повторный SELECT
SELECT balance FROM bank_accounts WHERE id = 3;
COMMIT;

-- Вопросы:
--   1) Почему оба SELECT возвращают одно и то же значение?
--   2) Почему SESSION A не видит новую версию строки после COMMIT в SESSION B?
--   3) Какие версии строки физически существуют на странице?
--   4) Как логика snapshot определяет видимость старой версии?
