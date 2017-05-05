import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by joana on 03/05/2017.
 */
public class ServerThread implements Runnable {

    private Socket s;
    private DataOutputStream sOut;
    private DataInputStream sIn;
    private Map<Byte, LinkedList<Integer>> gates;
    private byte gateID;
    private int numDoors;

    public ServerThread(Socket cli_s, Map<Byte, LinkedList<Integer>> gates) {
        s = cli_s;
        this.gates = gates;
    }

    public void run() {
        boolean timedOut = false;
        byte[] message = new byte[256];
        InetAddress clientIP;

        clientIP = s.getInetAddress();
        System.out.println("New client connection from " + clientIP.getHostAddress() + ", port number " + s.getPort());

        try {
            sOut = new DataOutputStream(s.getOutputStream());
            sIn = new DataInputStream(s.getInputStream());

            try {
              //  s.setSoTimeout(3000);
                sIn.read(message);

                System.out.println("Mensagem Tipo 1 recebida ");
                System.out.println(String.valueOf(message));

            } catch(SocketTimeoutException e) {
                System.out.println("Message reading timeout.");
                System.out.println("Client " + clientIP.getHostAddress() + ", port number: " + s.getPort() + " disconnected");
                s.close();

                timedOut = true;
            }

            if (!timedOut) {
                gateID = message[11];

                if (!gates.containsKey(gateID)) {
                    gates.put(gateID, new LinkedList<>());
                    numDoors = ((Byte) message[10]).intValue();
                    System.out.println("Gate " + message[11] + " was successfully registered.");

                    mainCommunicationFlow();
                }
            }

        } catch(IOException ex) {
            System.out.println("IOException");
        }
    }

    private void mainCommunicationFlow() {
        byte[] message = new byte[256];
        boolean timedOut = false;
        InetAddress clientIP;

        clientIP = s.getInetAddress();
        System.out.println("New new status request for " + clientIP.getHostAddress() + ", port number " + s.getPort());

        while (true) {
            /*try {
                Thread.sleep(45000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
*/
            try {
                sOut = new DataOutputStream(s.getOutputStream());
                sIn = new DataInputStream(s.getInputStream());

                sOut.write(1);
                sOut.write(0);
                sOut.write(ByteBuffer.allocate(2).putShort((short) 0).array());
                sOut.write(2);
                sOut.write(0);
                sOut.writeBytes(Timestamp.from(Instant.now()).toString());
                System.out.println("Mensagem Tipo 2 enviada");

                try {
                    s.setSoTimeout(5);
                    sIn.read(message);
                    System.out.println("Mensagem Tipo 3 recebida ");

                } catch (SocketTimeoutException e) {
                    System.out.println("Message reading timeout.");
                    System.out.println("Client " + clientIP.getHostAddress() + ", port number: " + s.getPort() + " disconnected");
                    s.close();

                    timedOut = true;
                }

                if (!timedOut) {
                    if (!gates.get(gateID).isEmpty()) {
                        gates.get(gateID).clear();
                    }

                    for (int i = 0; i < numDoors; i++) {
                        gates.get(gateID).add(((Byte) message[10 + i]).intValue());
                    }
                }

            } catch (IOException e) {
                System.out.println("IOException");
                break;
            }
        }
    }
}