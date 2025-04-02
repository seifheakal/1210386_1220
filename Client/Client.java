import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter device ID: ");
            String deviceId = scanner.nextLine();
            System.out.print("Enter command (TURN_ON/TURN_OFF): ");
            String command = scanner.nextLine().toUpperCase();
            HttpClient.sendCommand(deviceId, command);
        }
    }
}