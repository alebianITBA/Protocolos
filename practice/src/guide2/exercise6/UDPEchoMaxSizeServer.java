package guide2.exercise6;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by alebezdjian on 24/3/16.
 */
public class UDPEchoMaxSizeServer {
    public static final int MAX_SIZE = (63 * 1024) + 995;

    public static void main(String[] args) throws IOException {
        final int serverPort = 20007;
        DatagramSocket socket = new DatagramSocket(serverPort);
        DatagramPacket packet = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
        System.out.printf("Listening in port %d\n", serverPort);
        System.out.printf("Max size = %d bytes\n", MAX_SIZE);

        while (true) {
            socket.receive(packet);
            System.out.printf("Handling client at %s on port %d\n", packet.getAddress().getHostAddress(), packet.getPort());
            socket.send(packet);
            packet.setLength(MAX_SIZE);
        }
    }
}
