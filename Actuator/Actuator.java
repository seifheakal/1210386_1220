import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Actuator {
    private final Scanner scanner;

    public Actuator() {
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        while (true) {
            System.out.print("\nEnter actuator ID (or type 'exit' to quit): ");
            String id = scanner.nextLine();
            if (id.equalsIgnoreCase("exit")) break;

            System.out.print("Enter initial status (ON/OFF): ");
            String initialStatus = scanner.nextLine().toUpperCase();

            // Each sensor gets its own connection.
            new Thread(() -> registerAndListen(id, initialStatus)).start();
        }
    }

    private void registerAndListen(String id, String initialStatus) {
        try (Socket socket = new Socket("localhost", 8081);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to server for device: " + id);

            // Send registration message (format: id,status)
            out.println(id + "," + initialStatus);
            System.out.println("Sent registration: " + id + "," + initialStatus);

            // Maintain the current state locally.
            String currentStatus = initialStatus;

            while (true) {
                String command = in.readLine();
                if (command == null) break;

                String response;
                if (command.equals("TURN_ON")) {
                    if (currentStatus.equals("ON")) {
                        response = "Sensor with ID " + id + " was already ON";
                        System.out.println(response);
                    } else {
                        currentStatus = "ON";
                        response = id + " is ON";
                        System.out.println("Turning on " + id + ": " + response);
                    }
                } else if (command.equals("TURN_OFF")) {
                    if (currentStatus.equals("OFF")) {
                        response = "Sensor with ID " + id + " was already OFF";
                        System.out.println(response);
                    } else {
                        currentStatus = "OFF";
                        response = id + " is OFF";
                        System.out.println("Turning off " + id + ": " + response);
                    }
                } else if (command.equals("STATUS_REQUEST")) {
                    response = currentStatus;
                    System.out.println("Status request for " + id + ": " + response);
                } else if (command.equals("DUPLICATE")) {
                    System.out.println("Duplicate registration detected for sensor " + id + ". Closing connection.");
                    break;
                } else {
                    response = "Unknown command";
                    System.out.println("Received unknown command for " + id + ": " + command);
                }
                out.println(response);
            }
        } catch (IOException e) {
            System.out.println("Error with device " + id + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Actuator().start();
    }
}
