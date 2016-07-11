package ar.edu.itba.pdc.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import ar.edu.itba.pdc.connection.AdminConnection;
import ar.edu.itba.pdc.connection.DoubleBuffer;
import ar.edu.itba.pdc.parser.AdminCommand;
import ar.edu.itba.pdc.parser.AdminParser;
import ar.edu.itba.pdc.utils.ProxyConfiguration;

public class AdminHandler implements Handler {

    private static final ProxyConfiguration CONFIGURATION = ProxyConfiguration.INSTANCE;
    private static Logger LOGGER = Logger.getLogger(AdminHandler.class);

    private final Selector selector;
    private final AdminParser parser;
    private final AtomicBoolean finished;

    public AdminHandler(final Selector selector, final AtomicBoolean finished) {
        this.selector = selector;
        this.finished = finished;
        this.parser = new AdminParser();
    }

    @Override
    public void handleAccept(final SelectionKey key) throws IOException {
        final SocketChannel adminChannel = ((ServerSocketChannel) key.channel()).accept();

        final String address = adminChannel.socket().getRemoteSocketAddress().toString();

        LOGGER.info("Admin attempting connection from : " + address);

        // Connect to admin service
        adminChannel.configureBlocking(false);

        final AdminConnection adminConnection = new AdminConnection(adminChannel, selector,
                                                                    CONFIGURATION.getBufferSize(), finished);
        adminConnection.getBuffer().getWriteBuffer().append(AdminCommand.CONNECTED.success());

        adminChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, adminConnection);

        LOGGER.info("Accepted Admin connection -> " + address);
    }

    @Override
    public void handleRead(final SelectionKey key) throws IOException, InterruptedException {
        final SocketChannel channel = (SocketChannel) key.channel();
        final AdminConnection adminConnection = ((AdminConnection) key.attachment());
        final StringBuffer readBuffer = adminConnection.getBuffer().getReadBuffer();
        final ByteBuffer buffer = adminConnection.getByteBuffer();

        try {
            final long bytesRead = channel.read(buffer);
            buffer.flip();

            if (bytesRead == -1) {
                LOGGER.info("Admin disconnected:" + channel.socket().getRemoteSocketAddress());
                channel.close();
                adminConnection.close();
            } else if (bytesRead > 0) {
                final String line = DoubleBuffer.bufferToString(buffer);
                readBuffer.append(line);
                if (!line.endsWith("\n")) {
                    return;
                }
                final String fullLine = readBuffer.toString();
                readBuffer.delete(0, readBuffer.length());
                for (String s : fullLine.split("\r\n")) {
                    parser.parse(s, adminConnection);
                }
            }
        } catch (Exception e) {
            adminConnection.close();
        }
    }

    @Override
    public void handleWrite(final SelectionKey key) throws IOException {
        final AdminConnection adminConnection = (AdminConnection) key.attachment();
        final SocketChannel channel = adminConnection.getChannel();
        final StringBuffer writeBuffer = adminConnection.getBuffer().getWriteBuffer();
        final ByteBuffer buf = ByteBuffer.wrap(writeBuffer.toString().getBytes());

        try {
            final int bytesWritten = channel.write(buf);

            if (!buf.hasRemaining()) {
                key.interestOps(SelectionKey.OP_READ);
            }
            writeBuffer.delete(0, bytesWritten);

            buf.compact();
            buf.clear();
        } catch (Exception e) {
            adminConnection.close();
        }
    }
}
