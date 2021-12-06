import java.io.*;
import java.net.*;

public class SThread extends Thread
{
    private Socket clientSocket; // socket for communicating with a destination
    private PrintWriter clientWriter, routerWriter; // writers (for writing back to the machine and to destination)
    private BufferedReader clientReader, routerReader; // reader (for reading from the machine connected to)

    SThread(Socket clientSocket) throws IOException
    {
        this.clientSocket = clientSocket;
        InputStream inputStream = clientSocket.getInputStream();
        OutputStream outputStream = clientSocket.getOutputStream();
        clientWriter = new PrintWriter(outputStream);
        clientReader = new BufferedReader(new InputStreamReader(inputStream));
    }

    public boolean isServerRouter() {
        String clientType = null;
        try {
            clientType = clientReader.readLine();
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }

        if (clientType.equals("client")) {
            return false;
        } else if (clientType.equals("router")) {
            return true;
        }

        System.err.println("Error: Bad formed message by client SThread.isServerRouter()");
        System.exit(1);
        return false;
    }

    public void handleServer() {
        try {
            int index = Integer.parseInt(clientReader.readLine());
            String address = P2PRouter.getRouteAddress(index);
            clientWriter.print(address);
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    public String lookupSisterRouter(int index) {
        String address = null;
        try {
            Socket sock = new Socket(P2PRouter.sisterAddress, P2PRouter.sisterPort);
            routerReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            routerWriter = new PrintWriter(sock.getOutputStream());

            routerWriter.println("router");
            routerWriter.println(index);
            address = routerReader.readLine();
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }
        return address;
    }

    public void handleClient() {
        try {
            String task = clientReader.readLine();
            if (task.equals("lookup")) {
                int index = Integer.parseInt(clientReader.readLine());
                String address = P2PRouter.getRouteAddress(index);
                if (address == null) {
                    address = lookupSisterRouter(0);
                }
                clientWriter.println(address);
            } else if (task.equals("register")) {
                String address = clientReader.readLine();
                int index = P2PRouter.getRoutingIndex();
                P2PRouter.addRoute(index, address);
                clientWriter.println(index);
            } else {
                System.exit(1);
            }
        } catch (IOException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    // Run method (will run for each machine that connects to the ServerRouter)
    public void run() {
        if (isServerRouter()) {
            handleServer();
        } else {
            handleClient();
        }
    }
}