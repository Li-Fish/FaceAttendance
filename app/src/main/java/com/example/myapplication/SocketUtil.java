package com.example.myapplication;

import androidx.appcompat.widget.TintTypedArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class SocketUtil {
    static String serverName;
    static int port;
    static int groupID;


    public static String[] sendData(byte[] data) {
        String rst[] = new String[2];

        FishSocket client = new FishSocket(serverName, port);

        byte[] type = new byte[1];
        type[0] = '0';

        byte[] group_id = FishSocket.intToBytes(groupID);

        client.send(type);
        client.send(group_id);

        String code = "jECysvTpOkhQjsQg";
        client.send(code.getBytes());
        client.send(data);

        rst[0] = client.raw_recv();
        rst[1] = client.raw_recv();

        System.out.println(rst[0]);
        System.out.println(rst[1]);

        client.close();

        return rst;
    }

    public static boolean testServer(String inputIP, String inputPort, String inputGroupID, String code) {
        try {
            serverName = inputIP;
            port = Integer.parseInt(inputPort);

            FishSocket client = new FishSocket(serverName, port);

            byte[] type = new byte[1];
            type[0] = '2';
            groupID = Integer.parseInt(inputGroupID);
            byte[] group_id = FishSocket.intToBytes(groupID);
            client.send(type);
            client.send(group_id);
            client.send(code.getBytes());
            String rst = client.raw_recv();

            return rst.equals("OK");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

    public static byte[] intToBytes(int num) {
        byte[] rst = new byte[4];

        rst[0] = (byte) ((num >> 24) & 0x000FF);
        rst[1] = (byte) ((num >> 16) & 0x000FF);
        rst[2] = (byte) ((num >> 8) & 0x000FF);
        rst[3] = (byte) ((num >> 0) & 0x000FF);

        return rst;
    }

    void send(byte[] data) {
        try {
            byte[] lenData = intToBytes(data.length);
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