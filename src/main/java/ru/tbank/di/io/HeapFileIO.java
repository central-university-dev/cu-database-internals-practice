package ru.tbank.di.io;

import ru.tbank.di.memory.HeapPage;
import ru.tbank.di.memory.Page;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public final class HeapFileIO {
    private final Path path;

    public HeapFileIO(Path path) {
        this.path = path;
    }

    public void writePage(HeapPage page) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw");
             FileChannel ch = raf.getChannel()) {
            ch.position((long) page.getPageId() * Page.PAGE_SIZE);
            ch.write(java.nio.ByteBuffer.wrap(page.toBytes()));
            ch.force(true);
        }
    }

    public HeapPage readPage(int pageId) throws IOException {
        byte[] raw = new byte[Page.PAGE_SIZE];
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r");
             FileChannel ch = raf.getChannel()) {
            ch.position((long) pageId * Page.PAGE_SIZE);
            ByteBuffer b = ByteBuffer.wrap(raw);
            int n = ch.read(b);
            if (n != Page.PAGE_SIZE) throw new EOFException("short read");
        }
        return HeapPage.fromBytes(raw);
    }
}