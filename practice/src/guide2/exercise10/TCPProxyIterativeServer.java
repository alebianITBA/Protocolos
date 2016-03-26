package guide2.exercise10;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by alebezdjian on 25/3/16.
 */
public class TCPProxyIterativeServer {

    private ServerSocket clientServerSocket;
    private InetAddress forwardAddress;
    private int forwardPort;
    private ForwardConnectionHandler handler;

    public TCPProxyIterativeServer(final int thisPort, final InetAddress thisAddress, final int forwardPort, final InetAddress forwardAddress, ForwardConnectionHandler handler) throws IOException {
        this.clientServerSocket = new ServerSocket(thisPort, 50, thisAddress);
        this.forwardAddress = forwardAddress;
        this.forwardPort = forwardPort;
        this.handler = handler;
    }

    public void run() throws IOException {
        System.out.printf("Listening in %s\n", clientServerSocket.getLocalSocketAddress());
        System.out.printf("Forwarding to %s on port %d\n", forwardAddress.getHostName(), forwardPort);
        while (true) {
            // Wait for a client connection
            Socket clientSocket = clientServerSocket.accept();
            System.out.printf("Connection: %s\n", clientSocket.getRemoteSocketAddress().toString());

            Socket forwardSocket = new Socket(forwardAddress, forwardPort);
            System.out.println("Connected to forwarded server, sending client data");

            handler.handle(clientSocket, forwardSocket);

            closeSocket(forwardSocket);
            closeSocket(clientSocket);
        }
    }

    private void closeSocket(Socket socket) throws IOException {
        if (!socket.isClosed()) {
            socket.close();
            System.out.printf("Closing %s\n", socket.getRemoteSocketAddress().toString());
        }
        System.out.printf("%s disconnected\n", socket.getRemoteSocketAddress().toString());
    }

    public static void main(String[] args) {
        try {
            TCPProxyIterativeServer server = new TCPProxyIterativeServer(20000, InetAddress.getByName("localhost"), 20007, InetAddress.getByName("localhost"), new EchoForwardConnectionHandler());
            server.run();
        } catch (final Exception e) {
            System.out.println("Something went wrong");
            e.printStackTrace();
        }
    }
}
