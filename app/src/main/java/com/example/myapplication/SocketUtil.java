package com.example.myapplication;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class SocketUtil {
    public static String[] sendData(byte[] data) {
        String serverName = "192.168.123.136";
        int port = 11234;
        String rst[] = new String[2];

        FishSocket client = new FishSocket(serverName, port);

        byte[] type = new byte[1];
        type[0] = '0';

        byte[] group_id = new byte[1];
        group_id[0] = 1;

        client.send(type);
        client.send(group_id);

        String code = "mGHcTwFzkeZglvxa";
        client.send(code.getBytes());
        client.send(data);

        rst[0] = client.raw_recv();
        rst[1] = client.raw_recv();

        System.out.println(rst[0]);
        System.out.println(rst[1]);

        client.close();

        return rst;
    }
}

class FishSocket {
    OutputStream outToServer;
    InputStream inFromServer;
    BufferedReader bw;
    Socket client;

    FishSocket(String ip, int port) {
        try {
            System.out.println("连接到主机：" + ip + " ，端口号：" + port);
            client = new Socket(ip, port);
            System.out.println("远程主机地址：" + client.getRemoteSocketAddress());
            outToServer = client.getOutputStream();
            inFromServer = client.getInputStream();
            bw = new BufferedReader(new InputStreamReader(inFromServer));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void send(byte[] data) {
        try {
            byte[] lenData = new byte[4];
            int len = data.length;

            lenData[0] = (byte) ((len >> 24) & 0x000FF);
            lenData[1] = (byte) ((len >> 16) & 0x000FF);
            lenData[2] = (byte) ((len >> 8) & 0x000FF);
            lenData[3] = (byte) ((len >> 0) & 0x000FF);

            outToServer.write(lenData);
            outToServer.write(data);
            outToServer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String raw_recv() {
        try {
            return bw.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    void close() {
        try {
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}