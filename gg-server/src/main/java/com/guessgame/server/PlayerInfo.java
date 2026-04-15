package com.guessgame.server;

import java.net.Socket;
import java.io.*;

public class PlayerInfo {

    String username;
    String ip;
    int p2pPort;
    Socket socket;

    public PlayerInfo(String username, String ip, int p2pPort, Socket socket) {
        this.username = username;
        this.ip = ip;
        this.p2pPort = p2pPort;
        this.socket = socket;
    }

    public void send(String msg) throws IOException {
        PrintWriter out = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);

        out.println(msg);
    }
}
