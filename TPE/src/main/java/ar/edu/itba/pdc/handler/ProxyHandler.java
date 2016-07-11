package ar.edu.itba.pdc.handler;

import ar.edu.itba.pdc.connection.Connection;
import ar.edu.itba.pdc.connection.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public abstract class ProxyHandler implements Handler {
    private final Selector serverSelector;
    private final Selector clientSelector;

    ProxyHandler(final Selector clientSelector, final Selector serverSelector) {
        this.serverSelector = serverSelector;
        this.clientSelector = clientSelector;
    }

    public abstract void handleAccept(final SelectionKey key) throws IOException;

    public abstract void handleRead(final SelectionKey key) throws IOException, InterruptedException;

    public abstract void handleWrite(final SelectionKey key) throws IOException;

    public void writeToServer(final String commandLine, final Connection connection) {
        writeToChannel(connection.getServerChannel(), commandLine, serverSelector,
                connection.getServerBuffer().getWriteBuffer());
    }

    public void writeToClient(final String commandLine, final Connection connection) {
        writeToChannel(connection.getClientChannel(), commandLine, clientSelector,
                connection.getClientBuffer().getWriteBuffer());
    }

    private static void writeToChannel(final SocketChannel channel, final String commandLine,
                                      final Selector selector, final StringBuffer writeBuffer) {
        final SelectionKey key = channel.keyFor(selector);
        if (key == null || !key.isValid()) {
            return;
        }
        writeBuffer.append(commandLine);
        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    public void connectToServer(final Connection connection, final Server server) throws IOException {
        final SocketChannel serverSocketChannel = SocketChannel
                .open(new InetSocketAddress(server.getName(), server.getPort()));
        serverSocketChannel.configureBlocking(false);
        connection.connectToServer(serverSocketChannel);
        if (!serverSocketChannel.isOpen() || !serverSelector.isOpen()) {
            return;
        }
        serverSocketChannel.register(serverSelector, SelectionKey.OP_READ, connection);
    }
}
