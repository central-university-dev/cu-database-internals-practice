package ru.tbank.di.buffer;

import ru.tbank.di.io.HeapFileIO;
import ru.tbank.di.memory.HeapPage;
import ru.tbank.di.memory.Page;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MinimalBufferManager implements BufferManager {
    private final Map<Integer, Page> store = new HashMap<>();
    private final HeapFileIO io = new HeapFileIO(Path.of("data.heap"));

    @Override
    public Page get(int pid) throws IOException {
        if (!store.containsKey(pid)) {
            Page page = io.readPage(pid);
            store.put(pid, page);
        }
        return store.get(pid);
    }

    @Override
    public void write(int pid, HeapPage page) throws IOException {
        store.put(pid, page);
        io.writePage(page);
    }
}
