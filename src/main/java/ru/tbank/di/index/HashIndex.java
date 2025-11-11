package ru.tbank.di.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HashIndex {
    private HashIndexMetaPage meta;
    private Map<Integer, HashIndexPage> bucketPages;
    private List<HashIndexPage> overflowChains;  // overflow-цепочки

    public void insert(long keyHash, TID tid) {
        int bucket = meta.computeBucket(keyHash);

        // Получить или создать первичную страницу корзины
        // Попытка вставить в первичную страницу
    }

    public List<TID> search(long keyHash) {
        int bucket = meta.computeBucket(keyHash);

        HashIndexPage page = bucketPages.get(bucket);
        if (page == null) {
            return new ArrayList<>();  // Корзина не создана
        }

        // Ищем в первичной странице
        List<TID> results = page.searchByHash(keyHash);

        // Ищем в overflow-цепочке
        HashIndexPage overflowPage = page.getFirstOverflowPage();
        while (overflowPage != null) {
            results.addAll(overflowPage.searchByHash(keyHash));
            overflowPage = overflowPage.getNextOverflowPage();
        }

        return results;
    }

    private HashIndexPage allocateOrReuseOverflowPage(int bucket) {
        // Либо переиспользуем пустую (из bitmap), либо выделяем новую
        // Для простоты сейчас просто выделяем новую
        return new HashIndexPage();
    }

    private void performSplit() {
        // Раскол одной корзины (ленивый)
        // Буквально просто обновляем метастраницу
        // На практике раскол отложенный, может произойти позже
        meta.performSplit();
    }
}