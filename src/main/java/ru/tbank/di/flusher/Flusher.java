package ru.tbank.di.flusher;

import ru.tbank.di.buffer.MinimalBufferManager;

import java.io.IOException;

public class Flusher {
    private final MinimalBufferManager bm;
    private final int checkpointerInterval = 10000;
    private final int flusherInterval = 1000;
    private final int maxDirtyPages = 100;

    public Flusher(MinimalBufferManager bm) {
        this.bm = bm;
    }

    public void startCheckpointer() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(checkpointerInterval);
                    bm.flushAllDirty();
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void startFlusher() {
        new Thread(() -> {
            while (true) {
                try {
                    var dirtyPages = bm.getDirtyPages();
                    for (int i = 0; i < Math.min(dirtyPages.size(), maxDirtyPages); i++) {
                        bm.flushPage(dirtyPages.get(i).address);
                    }
                    Thread.sleep(flusherInterval);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
