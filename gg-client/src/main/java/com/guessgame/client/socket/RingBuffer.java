package com.guessgame.client.socket;

import java.nio.ByteBuffer;

public class RingBuffer {
    private int capacity;
    private byte delimiter;
    private int readHead;
    private int writeHead;
    private byte[] buffer;

    public RingBuffer(int capacity, byte delimiter) {
        this.capacity = capacity;
        this.delimiter = delimiter;
        this.readHead = 0;
        this.writeHead = 0;
        this.buffer = new byte[capacity];
    }

    public int getCapacity() {
        return capacity;
    }

    public int write(byte[] data, int length) {
        int bytesWritten = 0;
        for (int i = 0; i < length; i++) {
            if ((writeHead + 1) % capacity == readHead) {
                break;
            }
            buffer[writeHead] = data[i];
            writeHead = (writeHead + 1) % capacity;
            bytesWritten++;
        }
        return bytesWritten;
    }

    public int write(ByteBuffer data) {
        int bytesWritten = 0;
        while (data.hasRemaining()) {
            if ((writeHead + 1) % capacity == readHead) {
                break;
            }
            buffer[writeHead] = data.get();
            writeHead = (writeHead + 1) % capacity;
            bytesWritten++;
        }
        return bytesWritten;
    }

    public int read(byte[] dest, int length) {
        int bytesRead = 0;
        for (int i = 0; i < length; i++) {
            if (readHead == writeHead) {
                break;
            }
            dest[i] = buffer[readHead];
            readHead = (readHead + 1) % capacity;
            bytesRead++;
        }
        return bytesRead;
    }

    public int read(ByteBuffer dest) {
        int bytesRead = 0;
        while (dest.hasRemaining() && readHead != writeHead) {
            dest.put(buffer[readHead]);
            readHead = (readHead + 1) % capacity;
            bytesRead++;
        }
        return bytesRead;
    }

    public int rawRead(ByteBuffer dest) {
        int bytesRead = 0;
        int index = readHead;
        while (dest.hasRemaining() && index != writeHead) {
            dest.put(buffer[index]);
            index = (index + 1) % capacity;
            bytesRead++;
        }
        return bytesRead;
    }

    public void incReadHead(int length) {
        readHead = (readHead + length) % capacity;
    }

    public ByteBuffer getNextMessage() {
        if (readHead == writeHead) {
            return null;
        }

        int end = readHead;
        int length = 0;
        boolean find = false;

        while (end != writeHead) {
            if (buffer[end] == delimiter) {
                end = (end + 1) % capacity;
                length++;
                find = true;
                break;
            }
            end = (end + 1) % capacity;
            length++;
        }
        if (!find) {
            return null;
        }
        ByteBuffer message = ByteBuffer.allocate(length);
        for (int i = 0; i < length; i++) {
            message.put(buffer[readHead]);
            readHead = (readHead + 1) % capacity;
        }
        return message;
    }
}
