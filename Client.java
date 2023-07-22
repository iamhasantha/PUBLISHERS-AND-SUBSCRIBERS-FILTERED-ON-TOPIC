import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java Client <server-ip> <server-port> <client-type> <topic>");
            return;
        }

        String serverIP = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String clientType = args[2];
        String topic = args[3];

        try {
            Socket socket = new Socket(serverIP, serverPort);
            System.out.println("Connected to server: " + serverIP + ":" + serverPort);

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(clientType);
            writer.println(topic);

            if (clientType.equals("PUBLISHER")) {
                handlePublisher(socket, writer);
            } else if (clientType.equals("SUBSCRIBER")) {
                handleSubscriber(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handlePublisher(Socket socket, PrintWriter writer) {
        try (Scanner scanner = new Scanner(System.in)) {
            try {
                while (true) {
                    System.out.print("Enter a message to publish: ");
                    String message = scanner.nextLine();
                    writer.println(message);
                }
            }  finally {
                closeSocket(socket);
            }
        }
    }

    private static void handleSubscriber(Socket socket) {
        Scanner serverScanner = null;
        try {
            serverScanner = new Scanner(socket.getInputStream());
            while (true) {
                String message = serverScanner.nextLine();
                System.out.println("Received from server: " + message);

                if (message.equalsIgnoreCase("removeAll")) {
                    System.out.println("Unsubscribed from the server.");
                    break; 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverScanner != null) {
                serverScanner.close();
            }
            closeSocket(socket);
        }
    }

    private static void closeSocket(Socket socket) {
        try {
            socket.close();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
