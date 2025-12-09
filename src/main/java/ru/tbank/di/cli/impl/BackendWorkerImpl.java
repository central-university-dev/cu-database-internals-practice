package ru.tbank.di.cli.impl;

import ru.tbank.di.cli.api.BackendWorker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

// Backend-поток — аналог backend-процесса PostgreSQL
public class BackendWorkerImpl implements BackendWorker {

    private final Socket socket;
    private final EngineImpl engineImpl;

    public BackendWorkerImpl(Socket socket, EngineImpl engineImpl) {
        this.socket = socket;
        this.engineImpl = engineImpl;
    }

    @Override
    public void run() {
        System.out.println("Backend started for: " + socket.getRemoteSocketAddress());

        try (Socket s = socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))
        ) {
            writer.write("Welcome to MiniDB! Type 'exit' to quit.");
            writer.newLine();
            writer.flush();

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                    break;
                }

                if (line.isEmpty()) {
                    writer.write("__END__");
                    writer.newLine();
                    writer.flush();
                    continue;
                }

                String result = engineImpl.executeSql(line);
                for (String resLine : result.split("\\r?\\n")) {
                    writer.write(resLine);
                    writer.newLine();
                }
                writer.write("__END__");
                writer.newLine();
                writer.flush();
            }

            System.out.println("Client disconnected: " + socket.getInetAddress());
        } catch (IOException e) {
            System.err.println("Client connection error: " + e.getMessage());
        }
    }
}
