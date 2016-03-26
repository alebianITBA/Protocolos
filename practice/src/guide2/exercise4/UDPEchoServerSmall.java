package guide2.exercise4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by alebezdjian on 24/3/16.
 */
public class UDPEchoServerSmall {
    private static final int MAX_SIZE = 5;

    public static void main(String[] args) throws IOException {
        final int serverPort = 20007;
        DatagramSocket socket = new DatagramSocket(serverPort);
        DatagramPacket packet = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
        System.out.printf("Listening in port %d\n", serverPort);

        while (true) {
            socket.receive(packet);
            System.out.printf("Handling client at %s on port %d\n", packet.getAddress().getHostAddress(), packet.getPort());
            socket.send(packet);
            packet.setLength(MAX_SIZE);
        }
    }
}
