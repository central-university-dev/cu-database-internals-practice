package ru.tbank.di.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class HeapPage implements Page {
    private static final int MAGIC = 0xDBDBDB01;
    private static final short LEN_DELETED = (short)0xFFFF;
    private static final int SLOT_SIZE = 4;

    private final ByteBuffer buf;

    public HeapPage(int pageId) {
        this.buf = ByteBuffer
                .allocate(PAGE_SIZE)
                .order(ByteOrder.LITTLE_ENDIAN);

        buf.putInt(0, MAGIC);
        buf.putInt(4, pageId);
        buf.putInt(8, 0);                     // flags
        buf.putShort(12, (short) HEADER_SIZE);// lower
        buf.putShort(14, (short) PAGE_SIZE);  // upper
        buf.putShort(16, (short) 0);          // slotCount
        buf.putShort(18, (short) 1);          // version/reserved
        buf.putInt(20, 0);                    // checksum? (опц.)
    }

    private HeapPage(ByteBuffer existing) { this.buf = existing; }

    public static HeapPage fromBytes(byte[] raw) {
        if (raw.length != PAGE_SIZE) throw new IllegalArgumentException("bad size");
        ByteBuffer b = ByteBuffer
                .wrap(raw).order(ByteOrder.LITTLE_ENDIAN);
        if (b.getInt(0) != MAGIC) throw new IllegalStateException("bad magic");
        return new HeapPage(b);
    }

    @Override public int getPageId() { return buf.getInt(4); }

    private int lower() { return buf.getShort(12) & 0xFFFF; }
    private void setLower(int v) { buf.putShort(12, (short) v); }
    private int upper() { return buf.getShort(14) & 0xFFFF; }
    private void setUpper(int v) { buf.putShort(14, (short) v); }
    private int slotCount() { return buf.getShort(16) & 0xFFFF; }
    private void setSlotCount(int v) { buf.putShort(16, (short) v); }

    @Override public int getFreeSpace() { return upper() - lower(); }

    @Override public byte[] toBytes() {
        byte[] out = new byte[PAGE_SIZE];
        int p = buf.position();
        buf.rewind(); buf.get(out); buf.position(p);
        return out;
    }

    public int insert(byte[] row) {
        int need = SLOT_SIZE + row.length;
        if (getFreeSpace() < need) throw new IllegalStateException("no space");
        int newUpper = upper() - row.length;
        buf.position(newUpper); buf.put(row);

        int slotPos = lower();
        buf.putShort(slotPos, (short) newUpper);
        buf.putShort(slotPos + 2, (short) row.length);

        setLower(slotPos + SLOT_SIZE);
        setUpper(newUpper);
        setSlotCount(slotCount() + 1);
        return slotCount() - 1;
    }

    public byte[] read(int idx) {
        if (idx < 0 || idx >= slotCount()) throw new IndexOutOfBoundsException();
        int slotPos = HEADER_SIZE + idx * SLOT_SIZE;
        int off = buf.getShort(slotPos) & 0xFFFF;
        int len = buf.getShort(slotPos + 2) & 0xFFFF;
        if (len == (LEN_DELETED & 0xFFFF)) return null; // deleted
        byte[] out = new byte[len];
        int p = buf.position(); buf.position(off); buf.get(out); buf.position(p);
        return out;
    }

    public void delete(int idx) {
        if (idx < 0 || idx >= slotCount()) throw new IndexOutOfBoundsException();
        int slotPos = HEADER_SIZE + idx * SLOT_SIZE;
        buf.putShort(slotPos + 2, LEN_DELETED);
    }
}