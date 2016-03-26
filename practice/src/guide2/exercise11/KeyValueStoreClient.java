package guide2.exercise11;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by alebezdjian on 26/3/16.
 */
public class KeyValueStoreClient {
    public static void main(String[] args) throws IOException {
        sendRequest("SET-HOLA-0-\0");
        sendRequest("INC-HOLA-2-\0");
        sendRequest("DEC-HOLA-3-\0");
        sendRequest("GET-HOLA-\0");
        sendRequest("INC-CHAU-3-");
    }

    private static void sendRequest(String request) throws IOException {
        // Server name or IP address
        String server = "localhost";

        // Convert argument String to bytes using the default character encoding
        byte[] data = request.getBytes();
        int servPort = 20007;

        // Create socket that is connected to server on specified port
        Socket socket = new Socket(server, servPort);
        System.out.println("Connected to server. Sending Command: " + new String(data));

        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        out.write(data);

        byte[] response = new byte[64];
        in.read(response);

        System.out.println("Received: " + new String(response));

        socket.close();
    }
}
