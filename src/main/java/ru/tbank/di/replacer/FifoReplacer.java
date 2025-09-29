package ru.tbank.di.replacer;

import java.util.ArrayDeque;

public final class FifoReplacer<T> {
    private final ArrayDeque<T> queue = new ArrayDeque<>();

    public void add(T page) {
        if (queue.contains(page)) {
            return;
        }
        queue.addLast(page);
    }

    public void remove(T page) {
        queue.remove(page);
    }

    public T evictCandidate() {
        return queue.pollFirst();
    }
}
