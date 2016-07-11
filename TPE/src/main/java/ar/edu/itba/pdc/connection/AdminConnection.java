package ar.edu.itba.pdc.connection;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdminConnection {

    private static final Logger LOGGER = Logger.getLogger(AdminConnection.class);

    private final SocketChannel channel;
    private final Selector selector;
    private final DoubleBuffer buffer;
    private final ByteBuffer byteBuffer;
    private final AtomicBoolean finished;
    private final AtomicBoolean authenticated;

    public AdminConnection(final SocketChannel channel, final Selector selector,
                           final int bufferSize, final AtomicBoolean finished) {
        this.channel = channel;
        this.selector = selector;
        this.finished = finished;
        this.authenticated = new AtomicBoolean(false);
        this.buffer = new DoubleBuffer(bufferSize);
        this.byteBuffer = ByteBuffer.allocate(bufferSize);
    }

    public DoubleBuffer getBuffer() {
        return this.buffer;
    }

    public SocketChannel getChannel() {
        return this.channel;
    }

    public void finishServer() {
        finished.set(true);
    }

    public Selector getSelector() {
        return selector;
    }

    public boolean authenticated() {
        return authenticated.get();
    }

    public void authenticate() {
        this.authenticated.set(true);
    }

    public void logOut() {
        this.authenticated.set(false);
    }

    public void close() {
        try {
            final SelectionKey adminKey = channel.keyFor(selector);
            if (adminKey != null) {
                adminKey.cancel();
            }
            channel.close();
        } catch (IOException e) {
            LOGGER.error("Error closing Admin socket.");
        }
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }
}
