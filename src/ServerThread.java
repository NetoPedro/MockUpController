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
                //sIn.read(message);

                int a =0 ;
                System.out.println("Versio " + (sIn.readUnsignedByte() -48) );
                System.out.println("SebVersio " + (sIn.readUnsignedByte() -48) );
                a = sIn.readByte();
                a+= sIn.readByte()*256;
                System.out.println("Data_Lenght " + a  );
                System.out.println("Type " + (sIn.readUnsignedByte() -48) );
                System.out.println("Encryption " + (sIn.readUnsignedByte() -48) );
                int timeStamp = 0;
                timeStamp += sIn.readByte();
                timeStamp += sIn.readByte()*256;
                timeStamp += sIn.readByte()*256*256;
                timeStamp += sIn.readByte()*256*256*256;

                System.out.println("TimeStamp " + timeStamp );
                System.out.println("Doors number " + (sIn.readUnsignedByte() ));
                System.out.println("Gate ID " + (sIn.readUnsignedByte()));
                System.out.println("Mensagem Tipo 1 recebida ");
               // System.out.println(String.valueOf(message));

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
            try {
                Thread.sleep(45000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            try {
                sOut = new DataOutputStream(s.getOutputStream());
                sIn = new DataInputStream(s.getInputStream());
                sOut.write(1);
                sOut.write(0);
                sOut.write(0);
                sOut.write(0);

                //sOut.write(ByteBuffer.allocate(2).putShort((short) 0).array());
                sOut.write(2);
                sOut.write(0);
                int tt;
                int times = Timestamp.from(Instant.now()).getNanos();
                for (int j = 0; j<4;j++){
                    tt=times%256;
                    sOut.write(tt);
                    times /=256;
                }

                System.out.println("Mensagem Tipo 2 enviada");

                try {
                    s.setSoTimeout(5000);
                    //sIn.read(message);
                    int a =0 ;
                    System.out.println("Versio " + (sIn.readUnsignedByte() -48) );
                    System.out.println("SebVersio " +(sIn.readUnsignedByte() -48) );
                    a = sIn.readByte();
                    a+= sIn.readByte()*256;
                    System.out.println("Data_Lenght " + a  );
                    System.out.println("Type " + (sIn.readUnsignedByte() -48) );
                    System.out.println("Encryption " + (sIn.readUnsignedByte() -48) );
                    int timeStamp = 0;
                    timeStamp += sIn.readByte();
                    timeStamp += sIn.readByte()*256;
                    timeStamp += sIn.readByte()*256*256;
                    timeStamp += sIn.readByte()*256*256*256;

                    System.out.println("TimeStamp " + timeStamp );
                    for(int j = 0; j< a ; j++){
                        System.out.println("Door " + (sIn.readUnsignedByte()));
                    }
                    //System.out.println(message);
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