package ru.tbank.di.buffer;

import ru.tbank.di.memory.HeapPage;
import ru.tbank.di.memory.Page;

import java.io.IOException;

public interface BufferManager {
    Page get(PageAddress address) throws IOException;
    void write(PageAddress address, HeapPage page) throws IOException;
    void pin(PageAddress address);
    void unpin(PageAddress address);
}
