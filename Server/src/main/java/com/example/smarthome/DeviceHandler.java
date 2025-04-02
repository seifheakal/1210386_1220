package com.example.smarthome;

import java.io.*;
import java.net.Socket;

public class DeviceHandler {
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final Object lock = new Object();
    private final String deviceId;
    private String status;

    // Constructor reads the initial registration message (format: deviceId,status)
    public DeviceHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        System.out.println("Handling new device connection...");

        String registrationMessage = in.readLine();
        System.out.println("Received registration message: " + registrationMessage);

        if (registrationMessage == null || !registrationMessage.contains(",")) {
            throw new IOException("Invalid registration message.");
        }

        String[] parts = registrationMessage.split(",");
        if (parts.length < 2) {
            throw new IOException("Incomplete registration message.");
        }

        this.deviceId = parts[0];
        this.status = parts[1];
        System.out.println("Device ID: " + deviceId + ", Initial Status: " + status);
    }

    public String getDeviceId() {
        return deviceId;
    }

    // Synchronized method to send a command and wait for a response
    public String sendCommand(String command) throws IOException {
        synchronized (lock) {
            out.println(command);
            System.out.println("Sent command to " + deviceId + ": " + command);
            String response = in.readLine();
            System.out.println("Received response from " + deviceId + ": " + response);
            return response;
        }
    }

    public void close() {
        try {
            socket.close();
            System.out.println("Closed connection with " + deviceId);
        } catch (IOException e) {
            System.out.println("Error closing connection with " + deviceId + ": " + e.getMessage());
        }
    }
}
