import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Actuator {
    private String id;
    private String status;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public Actuator(String id, String initialStatus) {
        this.id = id;
        this.status = initialStatus.toUpperCase();
    }

    public void start() {
        try (Socket socket = new Socket("localhost", 8081);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            this.socket = socket;
            this.out = out;
            this.in = in;

            out.println(id + "," + status);

            while (true) {
                String command = in.readLine();
                if (command == null) break;

                if (command.equals("TURN_ON")) {
                    status = "ON";
                    out.println(id + " is ON");
                    System.out.println("Turned ON");
                } else if (command.equals("TURN_OFF")) {
                    status = "OFF";
                    out.println(id + " is OFF");
                    System.out.println("Turned OFF");
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter actuator ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter initial status (ON/OFF): ");
        String status = scanner.nextLine();

        Actuator actuator = new Actuator(id, status);
        actuator.start();
    }
}