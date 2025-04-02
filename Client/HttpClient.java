import java.io.*;
import java.net.*;
import java.util.Scanner;

public class HttpClient {
    public static void sendCommand(String deviceId, String command) {
        try {
            URL url = new URL("http://localhost:8080/control");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String data = "deviceId=" + URLEncoder.encode(deviceId, "UTF-8") +
                    "&command=" + URLEncoder.encode(command, "UTF-8");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = data.getBytes("UTF-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = br.readLine();
                System.out.println("Server response: " + response);
            } else {
                System.out.println("Error: HTTP " + responseCode);
            }

            conn.disconnect();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter device ID: ");
            String deviceId = scanner.nextLine();
            System.out.print("Enter command (TURN_ON/TURN_OFF): ");
            String command = scanner.nextLine().toUpperCase();
            sendCommand(deviceId, command);
        }
    }
}