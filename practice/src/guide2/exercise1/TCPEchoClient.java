package guide2.exercise1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by alebezdjian on 24/3/16.
 */
public class TCPEchoClient {
    public static void main(String[] args) throws IOException {
        String server = "localhost";
        byte[] message = "My message".getBytes();
        int serverPort = 20007;

        Socket socket = new Socket(server, serverPort);
        System.out.println("Connected to server. Sending message.");

        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        out.write(message);

        int totalBytesReceived = 0;
        int bytesReceived;
        byte[] oneByteReceived = new byte[1];

        System.out.println("Receiving...");

        while (totalBytesReceived < message.length) {
            bytesReceived = in.read(oneByteReceived);
            if (bytesReceived == -1) {
                throw new SocketException("Connection closed prematurely");
            }
            System.out.print(new String(oneByteReceived));
            totalBytesReceived += bytesReceived;
        }

        socket.close();
    }
}
