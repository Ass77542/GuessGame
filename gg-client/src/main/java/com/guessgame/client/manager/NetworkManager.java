package com.guessgame.client.manager;

import com.guessgame.client.socket.TCPServer;
import com.guessgame.client.socket.TCPClient;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class NetworkManager {
    private static NetworkManager instance;

    private TCPClient tcpClient;

    private NetworkManager() {
        tcpClient = new TCPClient();
    }

    public static synchronized NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public void flush() {
        tcpClient.flush();

    }

    public void setConnectionInfo(String ip, int port) {
        tcpClient.setIp(ip);
        tcpClient.setPort(port);
    }

    public void connect() {
        tcpClient.connect();
    }

    public void disconnect() {
        tcpClient.disconnect();
    }

    public void sendCommand(String command) {
        ByteBuffer buffer = ByteBuffer.wrap(command.getBytes());
        tcpClient.write(buffer);
    }

    public String receiveResponse() {
        ByteBuffer buffer = tcpClient.readNextMessage();
        if (buffer == null) {
            return null;
        }
        buffer.flip();
        String s = StandardCharsets.US_ASCII.decode(buffer).toString();
        return s;
    }
}
