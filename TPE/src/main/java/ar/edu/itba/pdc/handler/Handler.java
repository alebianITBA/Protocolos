package ar.edu.itba.pdc.handler;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface Handler {
    void handleAccept(SelectionKey key) throws IOException;
    void handleRead(SelectionKey key) throws IOException, InterruptedException;
    void handleWrite(SelectionKey key) throws IOException;
}
