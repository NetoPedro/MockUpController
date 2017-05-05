import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by joana on 03/05/2017.
 */
public class Controller {

    static InetAddress IPdestino;
    static ServerSocket sock;

    private static final  Map<Byte, LinkedList<Integer>> gates = new HashMap<>();

    public static void main(String args[]) throws Exception {
        Socket cliSock;

        try {
            sock = new ServerSocket(19999);
        } catch(IOException ex) {
            System.out.println("Failed to open server socket");
            System.exit(1);
        }

            while(true) {
                cliSock = sock.accept();
                new Thread(new ServerThread(cliSock, gates)).start();
            }
        }
    }