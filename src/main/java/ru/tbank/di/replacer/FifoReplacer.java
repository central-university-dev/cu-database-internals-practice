package ru.tbank.di.replacer;

import java.util.*;

public final class FifoReplacer {
    private final ArrayDeque<Integer> queue = new ArrayDeque<>();

    public void add(int pageId) {
        queue.addLast(pageId);
    }

    public void remove(int pageId) {
        queue.remove(pageId);
    }

    public Integer evictCandidate() {
        Integer victim = queue.pollFirst();
        if (victim != null) {
            queue.remove(victim);
        }
        return victim;
    }
}
