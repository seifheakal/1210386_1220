package com.example.smarthome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

@SpringBootApplication
@RestController
public class ServerApplication {
    private static final ConcurrentHashMap<String, Socket> deviceRegistry = new ConcurrentHashMap<>();
    private static final int TCP_PORT = 8081;

    public static void main(String[] args) {
        startTcpServer();
        SpringApplication.run(ServerApplication.class, args);
    }

    private static void startTcpServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {
                System.out.println("TCP Server started on port " + TCP_PORT);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> handleActuatorConnection(clientSocket)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void handleActuatorConnection(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String registrationMessage = in.readLine();
            String[] parts = registrationMessage.split(",");
            String deviceId = parts[0];
            String initialStatus = parts[1];

            deviceRegistry.put(deviceId, clientSocket);
            System.out.println("Device registered: " + deviceId + " with status: " + initialStatus);

            while (true) {
                String command = in.readLine();
                if (command == null) break;
                System.out.println("Received update from " + deviceId + ": " + command);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            deviceRegistry.values().remove(clientSocket);
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @PostMapping("/control")
    public String controlDevice(@RequestParam String deviceId, @RequestParam String command) {
        if (!deviceRegistry.containsKey(deviceId)) {
            return "{\"message\": \"Device not found\"}";
        }

        Socket deviceSocket = deviceRegistry.get(deviceId);
        try {
            PrintWriter out = new PrintWriter(deviceSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(deviceSocket.getInputStream()));

            out.println(command);
            String response = in.readLine();
            return "{\"message\": \"" + response + "\"}";
        } catch (IOException e) {
            deviceRegistry.remove(deviceId);
            return "{\"message\": \"Error communicating with device\"}";
        }
    }
}