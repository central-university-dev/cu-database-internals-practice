package ru.tbank.di.cli.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// Главный "руководящий" поток — аналог postmaster
public class ServerImpl {
    private static final int PORT = 5555;

    public static void main(String[] args) {
        System.out.println("MiniDB server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Принимаем новое соединение
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());

                // Для простоты создаём отдельный Engine на клиента
                EngineImpl engineImpl = new EngineImpl();

                // Создаём backend-поток, который будет обслуживать этого клиента
                Thread backend = new Thread(new BackendWorkerImpl(clientSocket, engineImpl));
                backend.setName("backend-" + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                backend.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
}
