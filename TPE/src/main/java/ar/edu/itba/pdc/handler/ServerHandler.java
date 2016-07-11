package ar.edu.itba.pdc.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import ar.edu.itba.pdc.connection.Connection;
import ar.edu.itba.pdc.connection.DoubleBuffer;
import ar.edu.itba.pdc.parser.ServerParser;
import ar.edu.itba.pdc.utils.ProxyConfiguration;

public class ServerHandler extends ProxyHandler {

    private static final ProxyConfiguration CONFIGURATION = ProxyConfiguration.INSTANCE;
    private static final Logger LOGGER = Logger.getLogger(ServerHandler.class);

    private final ServerParser serverParser;

    public ServerHandler(final Selector clientSelector, final Selector serverSelector) {
        super(clientSelector, serverSelector);
        this.serverParser = new ServerParser(this);
    }

    @Override
    public void handleAccept(final SelectionKey key) throws IOException {
        // This is empty because we are not going to receive requests from the servers
    }

    @Override
    public void handleRead(final SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        final Connection connection = ((Connection) key.attachment());
        final StringBuffer readBuffer = connection.getServerBuffer().getReadBuffer();
        final ByteBuffer buf = ByteBuffer.allocate(CONFIGURATION.getBufferSize());

        try {
            final long bytesRead = channel.read(buf);

            buf.flip();

            if (bytesRead == -1) {
                LOGGER.info("Server disconnected: " + connection.getServerChannel().socket().getRemoteSocketAddress());
                connection.close();
            } else if (bytesRead > 0) {
                final String line = DoubleBuffer.bufferToString(buf);
                buf.clear();
                readBuffer.append(line);
                if (!line.endsWith("\n")) {
                    return;
                }

                final String fullLine = readBuffer.toString();
                readBuffer.delete(0, readBuffer.length());
                serverParser.parse(fullLine, connection);
            }
        } catch (Exception e) {
            connection.close();
        }
    }

    @Override
    public void handleWrite(final SelectionKey key) throws IOException {
        final Connection connection = (Connection) key.attachment();
        final SocketChannel channel = connection.getServerChannel();
        if (!channel.isOpen()) {
            return;
        }
        final StringBuffer writeBuffer = connection.getServerBuffer().getWriteBuffer();
        final ByteBuffer buf = ByteBuffer.wrap(writeBuffer.toString().getBytes());

        try {
            final int bytesWritten = channel.write(buf);

            if (!buf.hasRemaining()) {
                // Nothing left, so no longer interested in writes
                key.interestOps(SelectionKey.OP_READ);
            }
            writeBuffer.delete(0, bytesWritten);
            // Make room for more data to be read in
            buf.compact();
            buf.clear();
        } catch (Exception e) {
            connection.close();
        }
    }
}
