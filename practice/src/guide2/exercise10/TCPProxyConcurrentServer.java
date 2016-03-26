package guide2.exercise10;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by alebezdjian on 25/3/16.
 */
public class TCPProxyConcurrentServer {

    private ServerSocket clientServerSocket;
    private InetAddress forwardAddress;
    private int forwardPort;
    private ForwardConnectionHandler handler;
    private static final int BUFFER_SIZE = 32;

    public TCPProxyConcurrentServer(final int thisPort, final InetAddress thisAddress, final int forwardPort, final InetAddress forwardAddress, ForwardConnectionHandler handler) throws IOException {
        this.clientServerSocket = new ServerSocket(thisPort, 50, thisAddress);
        this.forwardPort = forwardPort;
        this.forwardAddress = forwardAddress;
        this.handler = handler;
    }

    public void run() {
        System.out.printf("Listening in %s\n", clientServerSocket.getLocalSocketAddress());
        System.out.printf("Forwarding to %s on port %d\n", forwardAddress.getHostName(), forwardPort);
        while (true) {
            final Socket clientSocket;
            try {
                clientSocket = clientServerSocket.accept();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            new Thread(new Runnable() {
                public void run() {
                    try {
                        System.out.printf("Connection: %s\n", clientSocket.getRemoteSocketAddress().toString());
                        Socket forwardSocket = new Socket(forwardAddress, forwardPort);
                        System.out.println("Connected to forwarded server, sending client data");

                        handler.handle(clientSocket, forwardSocket);

                        closeSocket(forwardSocket);
                        closeSocket(clientSocket);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
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
            TCPProxyConcurrentServer server = new TCPProxyConcurrentServer(20000, InetAddress.getByName("localhost"), 20007, InetAddress.getByName("localhost"), new EchoForwardConnectionHandler());
            server.run();
        } catch (final Exception e) {
            System.out.println("Something went wrong");
            e.printStackTrace();
        }
    }
}
