package ru.tbank.di.analyzer;


import ru.tbank.di.catalog.Table;

public class RangeTblEntry {
    public Table table;        // ссылка на таблицу из каталога
    public String alias;       // псевдоним таблицы
    public int index;          // порядковый номер в запросе

}