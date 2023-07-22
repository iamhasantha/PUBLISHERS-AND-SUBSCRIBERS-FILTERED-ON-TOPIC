import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Server {
    private static final int PORT = 5001;
    private static Map<String, List<PrintWriter>> topicSubscribers = new HashMap<>();

    public static void main(String[] args) {
        try {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Server started and listening on port " + PORT);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket);

                    // Create a new thread for each client connection
                    Thread clientThread = new Thread(() -> handleClient(clientSocket));
                    clientThread.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try {
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            Scanner scanner = new Scanner(clientSocket.getInputStream());
            String clientType = scanner.nextLine();
            String topic = scanner.nextLine();

            System.out.println("Client type: " + clientType);
            System.out.println("Topic: " + topic);

            if (clientType.equals("PUBLISHER")) {
                handlePublisher(clientSocket, writer, topic);
            } else if (clientType.equals("SUBSCRIBER")) {
                handleSubscriber(clientSocket, writer, topic);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            removeClientConnection(clientSocket);
        }
    }

    // private static void handlePublisher(Socket clientSocket, PrintWriter writer, String topic) throws IOException {
    //     try (Scanner scanner = new Scanner(System.in)) {
    //         try {
    //             while (true) {
    //                 String message = scanner.nextLine();
    //                 String fullMessage = "[" + topic + "] " + message; // Include the topic in the message
    //                 System.out.println("Received from publisher: " + fullMessage);
    //                 broadcastMessage(topic, fullMessage);
    //             }
    //         } finally {
    //             closeSocket(clientSocket);
    //         }
    //     }
    // }

    private static void handlePublisher(Socket clientSocket, PrintWriter writer, String topic) throws IOException {
        try (Scanner scanner = new Scanner(clientSocket.getInputStream())) {
            try {
                while (scanner.hasNextLine()) {
                    String message = scanner.nextLine();
                    String fullMessage = "[" + topic + "] " + message; // Include the topic in the message
                    System.out.println("Received from publisher: " + fullMessage);
                    broadcastMessage(topic, fullMessage);
                }
            } finally {
                closeSocket(clientSocket);
            }
        }
    }
    

    // private static void handleSubscriber(Socket clientSocket, PrintWriter writer, String topic) throws IOException {
    //     try {
    //         synchronized (topicSubscribers) {
    //             List<PrintWriter> subscribers = topicSubscribers.getOrDefault(topic, new ArrayList<>());
    //             subscribers.add(writer);
    //             topicSubscribers.put(topic, subscribers);
    //         }

    //         while (true) {
    //             // keep the connection alive
    //         }
    //     } finally {
    //         synchronized (topicSubscribers) {
    //             List<PrintWriter> subscribers = topicSubscribers.get(topic);
    //             if (subscribers != null) {
    //                 subscribers.remove(writer);
    //                 if (subscribers.isEmpty()) {
    //                     topicSubscribers.remove(topic);
    //                 }
    //             }
    //         }
    //         closeSocket(clientSocket);
    //     }
    // }

    private static void handleSubscriber(Socket clientSocket, PrintWriter writer, String topic) throws IOException {
        try (Scanner scanner = new Scanner(clientSocket.getInputStream())) {
            synchronized (topicSubscribers) {
                List<PrintWriter> subscribers = topicSubscribers.getOrDefault(topic, new ArrayList<>());
                subscribers.add(writer);
                topicSubscribers.put(topic, subscribers);
            }
    
            while (scanner.hasNextLine()) {
                String message = scanner.nextLine();
                System.out.println("Received from server: " + message);
            }
        } finally {
            synchronized (topicSubscribers) {
                List<PrintWriter> subscribers = topicSubscribers.get(topic);
                if (subscribers != null) {
                    subscribers.remove(writer);
                    if (subscribers.isEmpty()) {
                        topicSubscribers.remove(topic);
                    }
                }
            }
            closeSocket(clientSocket);
        }
    }
    
    

    private static void broadcastMessage(String topic, String message) {
        synchronized (topicSubscribers) {
            List<PrintWriter> subscribers = topicSubscribers.get(topic);
            if (subscribers != null) {
                for (PrintWriter writer : subscribers) {
                    writer.println(message);
                }
            }
        }
    }

    private static void removeClientConnection(Socket clientSocket) {
        try {
            clientSocket.close();
            System.out.println("Client disconnected: " + clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
