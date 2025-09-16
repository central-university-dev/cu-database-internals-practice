package ru.tbank.di.buffer;

import ru.tbank.di.memory.HeapPage;
import ru.tbank.di.memory.Page;

import java.io.IOException;

public interface BufferManager {
    Page get(int pageId) throws IOException;
    void write(int pageId, HeapPage page) throws IOException;
    void pin(int pageId);
    void unpin(int pageId);
}
