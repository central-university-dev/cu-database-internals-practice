package ru.tbank.di.index;

public class HashIndexMetaPage {
    private int magic = 0xDEADBEEF;
    private int numBuckets;
    private int maxBucket;
    private int highMask;
    private int lowMask;
    private int recordCount;

    public int computeBucket(long hash) {
        // Вычислить корзину по маскам

        return bucket;
    }

    public void performSplit() {
        // При добавлении новой корзины
        // Если новый max_bucket = степень 2 - 1 (например, 31 = 2^5 - 1),
        // то расширяем маски на следующий цикл
    }

    // getters/setters...
    public int getMaxBucket() { return maxBucket; }
    public int getHighMask() { return highMask; }
    public int getLowMask() { return lowMask; }
    public void setMaxBucket(int mb) { maxBucket = mb; }
}