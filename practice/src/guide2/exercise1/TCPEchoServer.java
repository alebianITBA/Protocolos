package guide2.exercise1;

import utils.ConnectionHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by alebezdjian on 24/3/16.
 */
public class TCPEchoServer {
    private final ServerSocket server;

    public TCPEchoServer(final int port, final InetAddress address, final ConnectionHandler handler) throws IOException {
        System.out.println("Guide 2 Exercise 1 Server");
        this.server = new ServerSocket(port, 50, address);
        System.out.printf("Listening in %s\n", server.getLocalSocketAddress());
        while (true) {
            final Socket socket = server.accept();
            String s = socket.getRemoteSocketAddress().toString();

            System.out.printf("Connection from %s\n", s);
            handler.handle(socket);
            if (!socket.isClosed()) {
                socket.close();
                System.out.printf("Finished %s\n", s);
            }
        }
    }

    public static void main(String[] args) {
        try {
            new TCPEchoServer(20007, InetAddress.getByName("localhost"), new OneByteEchoHandler());
        } catch (final Exception e) {
            System.out.println("Something went wrong");
            e.printStackTrace();
        }
    }
}
