package ru.tbank.di.buffer;

import ru.tbank.di.io.HeapFileIO;
import ru.tbank.di.memory.HeapPage;
import ru.tbank.di.memory.Page;
import ru.tbank.di.replacer.FifoReplacer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinimalBufferManager implements BufferManager {
    private final Map<PageAddress, PageDescriptor> store = new HashMap<>();
    private final Map<Path, HeapFileIO> ioByPath = new HashMap<>();
    private final FifoReplacer<PageAddress> replacer = new FifoReplacer<>();
    private final int capacity;

    public MinimalBufferManager() {
        this(32);
    }

    public MinimalBufferManager(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
    }

    private HeapFileIO ioFor(Path file) throws IOException {
        Path normalized = file.toAbsolutePath().normalize();
        HeapFileIO io = ioByPath.get(normalized);
        if (io == null) {
            Path parent = normalized.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(normalized)) {
                Files.createFile(normalized);
            }
            io = new HeapFileIO(normalized);
            ioByPath.put(normalized, io);
        }
        return io;
    }

    @Override
    public synchronized Page get(PageAddress address) throws IOException {
        PageDescriptor desc = store.get(address);
        if (desc == null) {
            ensureFrame();
            HeapPage page = loadPage(address);
            desc = new PageDescriptor(address, page);
            store.put(address, desc);
            replacer.add(address);
        }

        desc.usageCount++;
        return desc.page;
    }

    private HeapPage loadPage(PageAddress address) throws IOException {
        HeapFileIO io = ioFor(address.file());
        long requiredSize = (long) (address.pageId() + 1) * Page.PAGE_SIZE;
        if (Files.size(address.file()) < requiredSize) {
            return new HeapPage(address.pageId());
        }
        return io.readPage(address.pageId());
    }

    @Override
    public synchronized void write(PageAddress address, HeapPage page) throws IOException {
        PageDescriptor desc = store.get(address);
        if (desc == null) {
            ensureFrame();
            desc = new PageDescriptor(address, page);
            store.put(address, desc);
            replacer.add(address);
        } else {
            desc.page = page;
        }

        desc.isDirty = true;
        HeapFileIO io = ioFor(address.file());
        io.writePage(page);
    }

    @Override
    public synchronized void pin(PageAddress address) {
        PageDescriptor desc = store.get(address);
        if (desc == null) {
            throw new IllegalArgumentException("Page not found in buffer: " + address);
        }
        replacer.remove(address);
        desc.pinCount++;
    }

    @Override
    public synchronized void unpin(PageAddress address) {
        PageDescriptor desc = store.get(address);
        if (desc == null) {
            throw new IllegalArgumentException("Page not found in buffer: " + address);
        }
        if (desc.pinCount > 0) {
            desc.pinCount--;
            if (desc.pinCount == 0) {
                replacer.add(address);
            }
        } else {
            replacer.add(address);
        }
    }

    private void ensureFrame() throws IOException {
        if (store.size() < capacity) return;

        PageAddress victimAddress = replacer.evictCandidate();
        if (victimAddress == null) {
            throw new IOException("No free frame: all pages are pinned");
        }

        PageDescriptor victim = store.remove(victimAddress);
        if (victim == null) {
            return;
        }

        if (victim.isDirty) {
            if (!(victim.page instanceof HeapPage heapPage)) {
                throw new IOException("Dirty page is not a HeapPage: " + victimAddress);
            }
            HeapFileIO io = ioFor(victimAddress.file());
            io.writePage(heapPage);
            victim.isDirty = false;
        }
    }

    public synchronized void flushAllDirty() throws IOException {
        for (PageDescriptor desc : getDirtyPages()) {
            flushDescriptor(desc);
        }
    }

    public synchronized void flushPage(PageAddress address) throws IOException {
        PageDescriptor desc = store.get(address);
        if (desc == null) {
            throw new IllegalArgumentException("Page not found in buffer: " + address);
        }
        flushDescriptor(desc);
    }

    private void flushDescriptor(PageDescriptor desc) throws IOException {
        if (!(desc.page instanceof HeapPage heapPage)) {
            throw new IllegalArgumentException("Dirty page is not a HeapPage: " + desc.address);
        }
        HeapFileIO io = ioFor(desc.address.file());
        io.writePage(heapPage);
        desc.isDirty = false;
    }

    public synchronized List<PageDescriptor> getDirtyPages() {
        return store.values().stream().filter(pageDescriptor -> pageDescriptor.isDirty).toList();
    }

    public static class PageDescriptor {
        public final PageAddress address;
        Page page;
        int usageCount = 0;
        int pinCount = 0;
        boolean isDirty = false;

        PageDescriptor(PageAddress address, Page page) {
            this.address = address;
            this.page = page;
        }
    }
}
