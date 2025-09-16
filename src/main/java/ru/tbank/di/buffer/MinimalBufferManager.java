package ru.tbank.di.buffer;

import ru.tbank.di.io.HeapFileIO;
import ru.tbank.di.memory.HeapPage;
import ru.tbank.di.memory.Page;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MinimalBufferManager implements BufferManager {
    private final Map<Integer, PageDescriptor> store = new HashMap<>();
    private final HeapFileIO io = new HeapFileIO(Path.of("data.heap"));

    private static class PageDescriptor {
        final int pageId;
        Page page;
        int usageCount = 0;
        int pinCount = 0;
        boolean isDirty = false;

        PageDescriptor(int pageId, Page page) {
            this.pageId = pageId;
            this.page = page;
        }
    }

    @Override
    public Page get(int pageId) throws IOException {
        PageDescriptor desc = store.get(pageId);

        if (desc == null) {
            Page page = io.readPage(pageId);
            desc = new PageDescriptor(pageId, page);
            store.put(pageId, desc);
        }

        desc.usageCount++;
        return desc.page;
    }

    @Override
    public void write(int pageId, HeapPage page) throws IOException {
        PageDescriptor desc = store.get(pageId);

        if (desc == null) {
            desc = new PageDescriptor(pageId, page);
            store.put(pageId, desc);
        } else {
            desc.page = page;
        }

        desc.isDirty = true;
        io.writePage(page);
    }

    public void pin(int pageId) {
        PageDescriptor desc = store.get(pageId);
        if (desc == null) {
            throw new IllegalArgumentException("Page not found in buffer: " + pageId);
        }

        desc.pinCount++;
    }

    public void unpin(int pageId) {
        PageDescriptor desc = store.get(pageId);
        if (desc == null) {
            throw new IllegalArgumentException("Page not found in buffer: " + pageId);
        }

        if (desc.pinCount > 0) {
            desc.pinCount--;
        }
    }
}
