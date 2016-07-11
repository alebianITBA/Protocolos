package ar.edu.itba.pdc.core;

import ar.edu.itba.pdc.handler.Handler;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class HandlerThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(HandlerThread.class);
    private static final int TIMEOUT = 10;

    private final Selector selector;
    private final Handler handler;
    private final AtomicBoolean finished;

    public HandlerThread(final Selector selector, final Handler handler,
                         final int port, final AtomicBoolean finished) {
        this.selector = selector;
        this.handler = handler;
        this.finished = finished;
        try {
            initializeSocketChannel(port);
        } catch (IOException e) {
            LOGGER.error("Error initializing socket channel.");
            e.printStackTrace();
        }
    }

    private void initializeSocketChannel(final int port) throws IOException {
        final ServerSocketChannel channel = ServerSocketChannel.open();
        channel.socket().bind(new InetSocketAddress(port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        try {
            while (!finished.get()) {
                // Run forever, processing available I/O operations
                // Wait for some channel to be ready (or timeout)
                if (selector.select(TIMEOUT) == 0) { // returns # of ready chans
                    continue;
                }
                handleKeys(selector, handler);
            }
        } catch (Exception e) {
            LOGGER.error("There was an error in the handler thread.");
            e.printStackTrace();
        }

        // Admin stopped server, close connections
        try {
            closeSelector();
        } catch (IOException e) {
            LOGGER.error("Error closing selector.");
            e.printStackTrace();
        }
    }

    private void closeSelector() throws IOException {
        selector.keys().stream().filter(key -> key.attachment() != null).forEach(SelectionKey::cancel);
        selector.close();
    }

    private void handleKeys(final Selector selector, final Handler handler) throws IOException, InterruptedException {

        // Get iterator on set of keys with I/O to process
        final Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();

        while (keyIter.hasNext()) {
            final SelectionKey key = keyIter.next(); // Key is bit mask
            // Server socket channel has pending connection requests?
            if (key.isValid() && key.isAcceptable())
                handler.handleAccept(key);

            // Client socket channel has pending data?
            if (key.isValid() && key.isReadable())
                handler.handleRead(key);

            // Client socket channel is available for writing?
            if (key.isValid() && key.isWritable())
                handler.handleWrite(key);

            keyIter.remove(); // remove from set after handling
        }
    }
}
