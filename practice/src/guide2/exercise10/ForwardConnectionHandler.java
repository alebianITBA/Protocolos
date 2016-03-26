package guide2.exercise10;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by alebezdjian on 26/3/16.
 */
public interface ForwardConnectionHandler {
    public void handle(Socket from, Socket to) throws IOException;
}
