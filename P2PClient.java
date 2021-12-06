import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class P2PClient {

    Socket socket; // socket to connect with ServerRouter
    PrintWriter out; // for writing to ServerRouter
    BufferedReader in; // for reading form ServerRouter
    InetAddress addr;
    String host; // Client machine's IP
    String routerName; // ServerRouter host name
    int sockNum; // port number

    public P2PClient(int socketNumber, String routerID) throws IOException {

        // Variables for setting up connection and communication
        this.socket = null; // socket to connect with ServerRouter
        this.out = null; // for writing to ServerRouter
        this.in = null; // for reading form ServerRouter
        this.addr = InetAddress.getLocalHost();
        this.host = addr.getHostAddress(); // Client machine's IP
        this.routerName = routerID; // ServerRouter host name
        this.sockNum = socketNumber; // port number

        this.initializeRouterConnection();
        this.start();
    }

    // Registers node with server router
    private void registerSelf() throws IOException {
        String address = this.addr.getHostAddress();
        this.out.println("REGISTER:" + address);
        this.out.println(address);
        String response = this.in.readLine(); //initial receive from router (verification of connection)
        System.out.println("ServerRouter: " + response);
    }

    // Requests address for node 'nodeId' from ServerRouter
    public String getAddressForNode(int nodeId) throws IOException {
        this.out.println("LOOKUP:" + nodeId); // Client sends the IP of its machine as initial send
        return this.in.readLine();
    }

    // Connect to router
    private void initializeRouterConnection() throws IOException {

        // Tries to connect to the ServerRouter
        try {
            this.socket = new Socket(routerName, this.sockNum);
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about router: " + routerName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + routerName);
            System.exit(1);
        }
        this.registerSelf();
    }

    public void start() throws IOException {
        String destinationAddress = this.getAddressForNode('x');
        Socket destinationConnection = new Socket(destinationAddress, this.sockNum);
        BufferedReader destinationIn = new BufferedReader(new InputStreamReader(destinationConnection.getInputStream()));
        PrintWriter destinationOut = new PrintWriter(destinationConnection.getOutputStream(), true);
        this.sendMessage(destinationConnection, destinationIn, destinationOut);
    }

    public void sendMessage(Socket dConnection, BufferedReader dIn, PrintWriter dOut) throws IOException {

        // Variables for message passing
        Reader reader = new FileReader("C:\\Users\\evanv\\OneDrive\\Desktop\\Parallel and Distributed Computing\\File5.txt");
        BufferedReader fromFile = new BufferedReader(reader); // reader for the string file
        String fromPeer;
        String toPeer;

        File av = new File("C:\\Users\\evanv\\OneDrive\\Desktop\\Parallel and Distributed Computing\\VideoFile4.avi");
        BufferedInputStream bis = null;
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(av);
            bis = new BufferedInputStream(fis);
            System.out.println("Wav file converted to bytes: ");
            while (bis.available() > 0) {
                System.out.print((char) bis.read());
            }
        } catch (FileNotFoundException fnfe) {
            System.out.println("Specified file not found " + fnfe);
        } catch (IOException ioe) {
            System.out.println("I/O Exception: " + ioe);
        } finally {
            try {
                if (bis != null && fis != null) {
                    fis.close();
                    bis.close();
                }
            } catch (IOException ioe) {
                System.out.println("Error in InputStream close(): " + ioe);
            }
        }
        long t0, t1, t;

        t0 = System.currentTimeMillis();

        // Communication while loop
        while ((fromPeer = dIn.readLine()) != null) {
            System.out.println("Peer: " + fromPeer);
            t1 = System.currentTimeMillis();
            if (fromPeer.equals("Bye.")) // exit statement
                break;
            t = t1 - t0;
            System.out.println("Cycle time: " + t);

            toPeer = fromFile.readLine(); // reading strings from a file
            if (toPeer != null) {
                System.out.println("Client: " + toPeer);
                out.println(toPeer); // sending the strings to the Server via ServerRouter
                t0 = System.currentTimeMillis();
            }
        }

        // Closing connections
        dOut.close();
        dIn.close();
        dConnection.close();
    }
}
