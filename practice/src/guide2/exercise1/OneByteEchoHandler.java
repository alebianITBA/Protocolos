package guide2.exercise1;

import utils.ConnectionHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by alebezdjian on 24/3/16.
 */
public class OneByteEchoHandler implements ConnectionHandler {

    private static final int BUFFER_SIZE = 1;

    @Override
    public void handle(Socket s) throws IOException {
        InputStream in = s.getInputStream();
        OutputStream out = s.getOutputStream();

        int receivedMessageSize;
        byte[] receivedBuffer = new byte[BUFFER_SIZE];

        while ((receivedMessageSize = in.read(receivedBuffer)) != -1) {
            out.write(receivedBuffer, 0, receivedMessageSize);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
