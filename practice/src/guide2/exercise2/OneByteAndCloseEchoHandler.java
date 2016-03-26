package guide2.exercise2;

import utils.ConnectionHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by alebezdjian on 24/3/16.
 */
public class OneByteAndCloseEchoHandler implements ConnectionHandler {

    private static final int BUFFER_SIZE = 1;

    @Override
    public void handle(Socket s) throws IOException {
        InputStream in = s.getInputStream();
        OutputStream out = s.getOutputStream();

        int receivedMessageSize;
        byte[] receivedBuffer = new byte[BUFFER_SIZE];

        receivedMessageSize = in.read(receivedBuffer);
        out.write(receivedBuffer, 0, receivedMessageSize);
    }
}
