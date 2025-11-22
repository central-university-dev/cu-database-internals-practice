-- ============================================================
-- Практика 2. Анализ JOIN order в PostgreSQL
-- Тема: порядок соединений, размеры таблиц, стоимость планов.
-- ============================================================

-- --------------------------------------------
-- 1. Инициализация данных
-- --------------------------------------------

DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS countries;

-- Таблица стран (200 строк)
CREATE TABLE countries(
                          id serial PRIMARY KEY,
                          name text
);

-- Таблица покупателей (10 000 строк)
CREATE TABLE customers(
                          id serial PRIMARY KEY,
                          name text,
                          age int,
                          country_id int REFERENCES countries(id)
);

-- Таблица заказов (1 000 000 строк)
CREATE TABLE orders(
                       id serial PRIMARY KEY,
                       customer_id int REFERENCES customers(id),
                       amount numeric(10,2),
                       created_at date
);


-- --------------------------------------------
-- 2. Наполнение таблицы countries — 200 строк
-- --------------------------------------------

INSERT INTO countries(name)
SELECT 'Country ' || i
FROM generate_series(1, 200) s(i);


-- --------------------------------------------
-- 3. Наполнение таблицы customers — 10 000 строк
--    Внешний ключ country_id всегда внутри диапазона 1..200
-- --------------------------------------------

INSERT INTO customers(name, age, country_id)
SELECT
    md5(random()::text),           -- случайное имя
    floor(random()*70)::int,       -- возраст 0..69
        floor(random()*200)::int + 1   -- валидный country_id
FROM generate_series(1, 10000);


-- --------------------------------------------
-- 4. Наполнение таблицы orders — 1 000 000 строк
--    Внешний ключ customer_id всегда внутри диапазона 1..10 000
-- --------------------------------------------

INSERT INTO orders(customer_id, amount, created_at)
SELECT
    floor(random()*10000)::int + 1,   -- валидный customer_id
        (random()*100)::numeric(10,2),    -- сумма заказа
                CURRENT_DATE - floor(random()*500)::int  -- дата в пределах 500 дней
FROM generate_series(1, 1000000);


-- --------------------------------------------
-- 5. Обновляем статистику для всех таблиц
-- --------------------------------------------

ANALYZE;


-- --------------------------------------------
-- 6. Исследуем план запроса
-- --------------------------------------------

EXPLAIN
SELECT *
FROM orders o
         JOIN customers c ON o.customer_id = c.id
         JOIN countries ct ON c.country_id = ct.id
WHERE c.age BETWEEN 30 AND 40;


-- ============================================================
-- Вопросы для анализа плана:
-- ============================================================

-- 1. С какого JOIN оптимизатор начинает выполнение?
--    Подсказка: посмотрите на узлы плана от вложенных к внешним.

-- 2. Почему PostgreSQL НЕ начинает с самой большой таблицы orders
--    (в которой 1 000 000 строк)?

-- 3. Какой JOIN выполняется первым:
--       customers ⋈ countries
--       или orders ⋈ customers ?
--    Почему именно такой порядок?

-- 4. Какой тип JOIN используется (Hash Join, Nested Loop, Merge Join)?
--    Почему оптимизатор считает его оптимальным в данной ситуации?

-- 5. Как влияет размер промежуточного результата на стоимость
--    следующего JOIN?
--
--    Подумайте:
--      - customers ≈ 10 000 строк
--      - countries ≈ 200 строк
--      - orders ≈ 1 000 000 строк
--    Какое соединение выгоднее выполнить первым?
