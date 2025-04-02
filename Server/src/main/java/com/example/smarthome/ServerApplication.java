package com.example.smarthome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@RestController
public class ServerApplication {
    private static final ConcurrentHashMap<String, DeviceHandler> deviceRegistry = new ConcurrentHashMap<>();
    private static final int TCP_PORT = 8081;

    public static void main(String[] args) {
        startTcpServer();
        startPeriodicStatusLogger();
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
        try {
            System.out.println("New connection from: " + clientSocket.getInetAddress());

            DeviceHandler deviceHandler = new DeviceHandler(clientSocket);
            String deviceId = deviceHandler.getDeviceId();

            // Check for duplicate registration.
            if (deviceRegistry.containsKey(deviceId)) {
                System.out.println("Device with ID " + deviceId + " is already registered. Rejecting duplicate.");
                deviceHandler.sendCommand("DUPLICATE");
                deviceHandler.close();
                return;
            }

            deviceRegistry.put(deviceId, deviceHandler);
            System.out.println("Device registered: " + deviceId + " with initial status: "
                    + deviceHandler.sendCommand("STATUS_REQUEST"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startPeriodicStatusLogger() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.print("[Status Snapshot] ");
                for (Map.Entry<String, DeviceHandler> entry : deviceRegistry.entrySet()) {
                    String id = entry.getKey();
                    DeviceHandler handler = entry.getValue();
                    try {
                        String status = handler.sendCommand("STATUS_REQUEST");
                        System.out.print(id + ": " + status + ", ");
                    } catch (IOException e) {
                        System.out.print(id + ": error, ");
                    }
                }
                System.out.println();
            }
        }).start();
    }

    // HTTP endpoint to control the device
    @PostMapping("/control")
    public String controlDevice(@RequestParam String deviceId, @RequestParam String command) {
        if (!deviceRegistry.containsKey(deviceId)) {
            return "{\"message\": \"Device not found\"}";
        }
        DeviceHandler deviceHandler = deviceRegistry.get(deviceId);
        try {
            String response = deviceHandler.sendCommand(command);
            return "{\"message\": \"" + response + "\"}";
        } catch (IOException e) {
            deviceRegistry.remove(deviceId);
            return "{\"message\": \"Error communicating with device\"}";
        }
    }
}
