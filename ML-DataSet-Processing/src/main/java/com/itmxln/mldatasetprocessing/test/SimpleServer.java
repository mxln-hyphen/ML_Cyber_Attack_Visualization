package com.itmxln.mldatasetprocessing.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleServer {
    public static void main(String[] args) {
        int port = 80; // 监听的端口号
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("服务器正在监听端口: " + port);

            // 服务器无限循环等待客户端连接
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("客户端已连接: " + clientSocket.getInetAddress().getHostAddress());

                    // 读取客户端发送的消息
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String line = in.readLine();
                    System.out.println("收到客户端的消息: " + line);
                } catch (Exception e) {
                    System.out.println("处理客户端请求时发生错误: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("服务器异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
