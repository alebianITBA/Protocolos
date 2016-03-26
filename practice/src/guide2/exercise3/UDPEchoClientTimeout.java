package guide2.exercise3;

import guide2.exercise6.UDPEchoMaxSizeServer;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.io.InterruptedIOException;

public class UDPEchoClientTimeout {

    private static final int TIMEOUT = 3000; // Resend timeout (milliseconds)
    private static final int MAXTRIES = 3; // Maximum retransmissions

    public static void main(String[] args) throws IOException {
        InetAddress serverAddress = InetAddress.getByName("localhost"); // Server
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < UDPEchoMaxSizeServer.MAX_SIZE; i++) {
            if (i % 150 == 0) {
                string.append("\n");
            } else {
                string.append("A");
            }
        }
        byte[] bytesToSend = string.toString().getBytes();
        int servPort = 20007;

        DatagramSocket socket = new DatagramSocket();

        socket.setSoTimeout(TIMEOUT); // Maximum receive blocking time
        // (milliseconds)

        DatagramPacket sendPacket = new DatagramPacket(bytesToSend, // Sending
                // packet
                bytesToSend.length, serverAddress, servPort);

        DatagramPacket receivePacket = // Receiving packet
                new DatagramPacket(new byte[bytesToSend.length], bytesToSend.length);

        int tries = 0; // Packets may be lost, so we have to keep trying
        boolean receivedResponse = false;
        do {
            socket.send(sendPacket); // Send the echo string
            try {
                socket.receive(receivePacket); // Attempt echo reply reception
                if (!receivePacket.getAddress().equals(serverAddress)) {// Check
                    // source
                    throw new IOException("Received packet from an unknown source");
                }
                receivedResponse = true;
            } catch (InterruptedIOException e) { // We did not get anything
                tries += 1;
                System.out.println("Timed out, " + (MAXTRIES - tries) + " more tries...");
            }
        } while ((!receivedResponse) && (tries < MAXTRIES));

        if (receivedResponse) {
            System.out.println("Received:\n" + new String(receivePacket.getData()));
        } else {
            System.out.println("No response -- giving up.");
        }
        socket.close();
    }
}