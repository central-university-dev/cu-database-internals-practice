package ru.tbank.di.cli.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ClientImpl {

    private static final String HOST = "localhost";
    private static final int PORT = 5555;

    public static void main(String[] args) {
        System.out.println("Connecting to MiniDB Server at " + HOST + ":" + PORT + "...");

        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader reader =
                     new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer =
                     new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
             Scanner scanner = new Scanner(System.in)
        ) {

            // Читаем welcome сообщение от сервера
            String welcome = reader.readLine();
            System.out.println(welcome);

            // Основной цикл клиента
            while (true) {
                System.out.print("client-sql> ");
                String line = scanner.nextLine().trim();

                // Отправляем серверу
                writer.write(line);
                writer.newLine();
                writer.flush();

                if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                    break;
                }

                // Читаем ответ от сервера до маркера __END__
                String responseLine;
                while ((responseLine = reader.readLine()) != null) {
                    if (responseLine.equals("__END__")) {
                        break;
                    }
                    System.out.println(responseLine);
                }

                if (responseLine == null) { // сервер закрыл соединение
                    System.out.println("Server closed connection.");
                    break;
                }
            }

            System.out.println("Disconnected from server.");
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }
}
