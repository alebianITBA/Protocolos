package guide2.exercise10;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by alebezdjian on 26/3/16.
 */
public class EchoForwardConnectionHandler implements ForwardConnectionHandler {

    private static final int BUFFER_SIZE = 32;
    private byte[] clientBuffer = new byte[BUFFER_SIZE];
    private byte[] serverBuffer = new byte[BUFFER_SIZE];

    @Override
    public void handle(Socket clientSocket, Socket forwardSocket) throws IOException {
        InputStream clientIn = clientSocket.getInputStream();
        OutputStream clientOut = clientSocket.getOutputStream();

        int clientMessageSize;

        while ((clientMessageSize = clientIn.read(clientBuffer)) != -1) {
            System.out.println("Received from cliente: " + new String(clientBuffer));

            InputStream serverIn = forwardSocket.getInputStream();
            OutputStream serverOut = forwardSocket.getOutputStream();

            serverOut.write(clientBuffer, 0, clientMessageSize);

            int totalBytesReceivedFromServer = receiveMessageFromForwardedServer(clientMessageSize, serverIn);

            System.out.println("Received from server: " + new String(serverBuffer));
            clientOut.write(serverBuffer, 0, totalBytesReceivedFromServer);
        }
    }

    private int receiveMessageFromForwardedServer(int length, InputStream serverInput) throws IOException {
        int totalBytesReceived = 0;
        int bytesRcvd;
        // Bytes received in last read
        while (totalBytesReceived < length) {
            if ((bytesRcvd = serverInput.read(serverBuffer, totalBytesReceived, serverBuffer.length - totalBytesReceived)) == -1)
                throw new SocketException("Connection closed prematurely");
            totalBytesReceived += bytesRcvd;
        }
        return totalBytesReceived;
    }
}
