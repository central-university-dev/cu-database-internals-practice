package ru.tbank.di.memory;

public interface Page {
    int PAGE_SIZE = 8192;
    int HEADER_SIZE = 24;
    int getPageId();
    int getFreeSpace();
    byte[] toBytes();
}