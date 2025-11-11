package ru.tbank.di.index;

public class HashIndexPage {
    static class IndexRecord implements Comparable<IndexRecord> {
        long hash;
        TID tid;

        public int compareTo(IndexRecord other) {
            return Long.compare(this.hash, other.hash);
        }
    }

    public void insertRecordSorted(long hash, TID tid) {
        // Вставить запись, сохраняя упорядоченность по хешу
        IndexRecord rec = new IndexRecord();
        rec.hash = hash;
        rec.tid = tid;

        // Найти позицию в отсортированном порядке
        int pos = binarySearchInsertPosition(hash);
    }

    public List<TID> searchByHash(long hash) {
        // Двоичный поиск для всех совпадающих хешей
        List<TID> results = new ArrayList<>();

        int leftPos = binarySearchFirst(hash);
        if (leftPos < 0) {
            return results;  // Не нашли
        }

        // Добавляем все записи с совпадающим хешем

        return results;
    }

    private int binarySearchInsertPosition(long hash) {
        // Двоичный поиск позиции для вставки
        // ...implementation
    }

    private int binarySearchFirst(long hash) {
        // Двоичный поиск первой записи с данным хешем
        // ...implementation
    }
}