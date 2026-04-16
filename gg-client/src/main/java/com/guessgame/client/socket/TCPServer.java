package com.guessgame.client.socket;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.InetSocketAddress;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.ByteBuffer;

import java.util.Map;
import java.util.HashMap;

public class TCPServer {
    /*
    private int port;
    private String ip;

    private InetAddress inetAddress;
    private SocketAddress socketAddress;
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    private record ClientInfo(RingBuffer readBuffer, RingBuffer writeBuffer) {}
    private Map<SocketChannel, ClientInfo> clientBuffers;

    private boolean isRunning = false;

    public TCPServer() {}

    public TCPServer(String ip, int port) {
        this.port = port;
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        if (isRunning) {
            throw new IllegalStateException("Cannot change port while server is running");
        }
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        if (isRunning) {
            throw new IllegalStateException("Cannot change IP while server is running");
        }
        this.ip = ip;
    }

    public void listen() {
        this.clientBuffers = new HashMap<>();
        try {
            this.inetAddress = InetAddress.getByName(ip);
            this.socketAddress = new InetSocketAddress(inetAddress, port);

            this.serverSocketChannel = ServerSocketChannel.open();
            this.serverSocketChannel.bind(socketAddress);
            this.serverSocketChannel.configureBlocking(false);

            this.selector = Selector.open();
            this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize TCPServer: " + e.getMessage(), e);
        }
        isRunning = true;
    }

    public void close() {
        if (!isRunning) {
            return;
        }
        if (clientBuffers != null) {
            for (SocketChannel channel : clientBuffers.keySet()) {
                try {
                    channel.close();
                } catch (Exception e) {
                    System.err.println("Failed to close client channel: " + e.getMessage());
                }
            }
            clientBuffers.clear();
        }
        try {
            if (selector != null) selector.close();
            if (serverSocketChannel != null) serverSocketChannel.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to close TCPServer: " + e.getMessage(), e);
        }
        isRunning = false;
    }

    private void accept(SelectionKey key) {
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = serverChannel.accept();

            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            clientChannel.register(selector, SelectionKey.OP_WRITE);
            clientBuffers.put(clientChannel, new ClientInfo(new RingBuffer(1024, (byte) '\n'), new RingBuffer(1024, (byte) '\n')));
        } catch(Exception e) {
            throw new RuntimeException("Failed to accept connection: " + e.getMessage(), e);
        }
    }

    private void read(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.clear();

        try {
            long bytesRead = clientChannel.read(buffer);
            if (bytesRead == -1) {
                clientChannel.close();
                key.cancel();
                return;
            }
            RingBuffer readBuffer = clientBuffers.get(clientChannel).readBuffer();
            readBuffer.write(buffer.array(), (int) bytesRead);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read from client: " + e.getMessage(), e);
        }
    }

    private void write(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        try {
            RingBuffer writeBuffer = clientBuffers.get(clientChannel).writeBuffer();
            ByteBuffer message = writeBuffer.getNextMessage();
            if (message != null) {
                clientChannel.write(message);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to write to client: " + e.getMessage(), e);
        }

    }

    private void serveOnce() {
        ClientInfo clientInfo;
        try {
            selector.select();
       } catch (Exception e) {
            throw new RuntimeException("Failed to select: " + e.getMessage(), e);
        }

        for (SelectionKey key : selector.selectedKeys()) {
            if (key.isAcceptable()) {
                accept(key);
            }
            if (key.isReadable()) {
                read(key);
            }
            if (key.isWritable()) {
                write(key);
            }
       }
    }
*/
};
