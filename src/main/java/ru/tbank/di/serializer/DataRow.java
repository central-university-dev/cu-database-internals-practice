package ru.tbank.di.serializer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public record DataRow(Long id, String name) {

    public byte[] toBytes() {
        byte mask = 0; int size = 1;
        byte[] nameBytes = null;

        if (id != null) { size += 8; mask |= 0b0000_0001; }
        if (name != null) {
            nameBytes = name.getBytes(StandardCharsets.UTF_8);
            size += 4 + nameBytes.length; mask |= 0b0000_0010;
        }

        ByteBuffer b = ByteBuffer
                .allocate(size)
                .order(ByteOrder.LITTLE_ENDIAN);

        b.put(mask);
        if ((mask & 1) != 0) b.putLong(id);
        if ((mask & 2) != 0) { b.putInt(nameBytes.length); b.put(nameBytes); }
        return b.array();
    }

    public static DataRow fromBytes(byte[] raw) {
        ByteBuffer b = ByteBuffer
                .wrap(raw)
                .order(ByteOrder.LITTLE_ENDIAN);

        byte mask = b.get();
        Long id = null; String name = null;

        if ((mask & 1) != 0) id = b.getLong();
        if ((mask & 2) != 0) {
            int len = b.getInt();
            if (len < 0 || b.remaining() < len) throw new IllegalStateException("bad varchar");
            byte[] nb = new byte[len];
            b.get(nb);
            name = new String(nb, StandardCharsets.UTF_8);
        }
        return new DataRow(id, name);
    }
}