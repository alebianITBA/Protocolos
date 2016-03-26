package utils;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by alebezdjian on 24/3/16.
 */
public interface ConnectionHandler {
    void handle(Socket s) throws IOException;
}
