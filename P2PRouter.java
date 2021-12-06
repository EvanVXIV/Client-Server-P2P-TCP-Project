import java.net.*;
import java.io.*;
import java.util.HashMap;

public class P2PRouter {
    static HashMap<Integer, String> routingTable; // routing table

    static ServerSocket serverSocket;
    static int port;
    static String sisterAddress;
    static int sisterPort;
    static int routingIndex;
    static boolean running;

    public static void main(String[] args) throws IOException {
        port = Integer.parseInt(args[0]);
        sisterAddress = args[1];
        sisterPort = Integer.parseInt(args[2]);
        routingTable = new HashMap<>();

        // Initialize server
        try {
            serverSocket = new ServerSocket(5555);
            System.out.println("ServerRouter is Listening on port: " + port + ".");
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port + ".");
            System.exit(1);
        }

        // Creating threads with accepted connections
        while (running == true)
        {
            try {
                Socket clientSocket = serverSocket.accept();
                SThread t = new SThread(clientSocket); // creates a thread with a random port
                t.start();
                System.out.println("ServerRouter connected with Client/Server: " + clientSocket.getInetAddress().getHostAddress());
            } catch (IOException e) {
                System.err.println("Client/Server failed to connect.");
                System.exit(1);
            }
        }
        serverSocket.close();
    }

    public static synchronized int getRoutingIndex() {
        return routingIndex++;
    }

    public static void addRoute(int index, String address) {
        synchronized (routingTable) {
            routingTable.put(index, address);
        }
    }

    public static String getRouteAddress(int index) {
        synchronized (routingTable) {
            return routingTable.get(index);
        }
    }
}