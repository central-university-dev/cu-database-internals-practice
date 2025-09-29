package ru.tbank.di.buffer;

import java.nio.file.Path;
import java.util.Objects;

public record PageAddress(Path file, int pageId) {

    public PageAddress {
        Objects.requireNonNull(file, "file");
        file = file.toAbsolutePath().normalize();
        if (pageId < 0) {
            throw new IllegalArgumentException("pageId must be non-negative");
        }
    }
}
