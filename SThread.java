import java.io.*;
import java.net.*;

public class SThread extends Thread
{
    private Object [][] RTable; // routing table
    private PrintWriter out, outTo; // writers (for writing back to the machine and to destination)
    private BufferedReader in; // reader (for reading from the machine connected to)
    private String inputLine, outputLine, destination, addr; // communication strings
    private Socket outSocket; // socket for communicating with a destination
    public int ind; // index in the routing table

    SThread(Object [][] Table) throws IOException
    {
        RTable = Table;
    }

    public int insertSocket(Socket connection, int index) throws IOException{
        out = new PrintWriter(connection.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        addr = connection.getInetAddress().getHostAddress();
        boolean found = false;
        for(int i = 0; i < RTable.length; i++){
            if(RTable[i][0] == addr){
                if(!((Socket)RTable[i][1]).isConnected()){
                    return 0;
                }else{
                    RTable[i][1] = connection;
                    return 0;
                }
            }
        }
        RTable[index][0] = addr; // IP addresses
        RTable[index][1] = connection; // sockets for communication
        return 1;
    }

    // Run method (will run for each machine that connects to the ServerRouter)
    public void run()
    {
        try
        {
            // Initial sends/receives
            destination = in.readLine(); // initial read (the destination for writing)
            System.out.println("| Forwarding to " + destination);
            out.println("Connected to the router."); // confirmation of connection

            boolean found = false;
            // loops through the routing table to find the destination
            while(!found){
                Thread.currentThread().sleep(5000);
                for (int i = 0; i < RTable.length; i++)
                {
                    if (destination.equals((String) RTable[i][0])){
                        outSocket = (Socket) RTable[i][1]; // gets the socket for communication from the table
                        System.out.println("| Found destination: " + destination);
                        outTo = new PrintWriter(outSocket.getOutputStream(), true); // assigns a writer
                        found = true;
                        break;
                    }
                }
            }

            // Communication loop
            while ((inputLine = in.readLine()) != null) {
                System.out.println("| Client/Server said: " + inputLine);
                outputLine = inputLine; // passes the input from the machine to the output string for the destination
                if ( outSocket != null){
                    outTo.println(outputLine); // writes to the destination
                }
                if (inputLine.equals("Bye.")) // exit statement
                    break;
            }// end while
        }// end try
        catch (IOException e) {
            System.err.println("| Could not listen to socket.\nReason: " + e.toString());
            System.exit(1);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}