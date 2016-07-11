package ar.edu.itba.pdc.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import ar.edu.itba.pdc.connection.Connection;
import ar.edu.itba.pdc.connection.DoubleBuffer;
import ar.edu.itba.pdc.parser.ClientParser;
import ar.edu.itba.pdc.parser.POP3Command;
import ar.edu.itba.pdc.statistics.Statistics;

public class ClientHandler extends ProxyHandler {

	private static Logger LOGGER = Logger.getLogger(ClientHandler.class);

	private Selector clientSelector;
	private ClientParser clientParser;

	public ClientHandler(final Selector clientSelector, final Selector serverSelector) {
		super(clientSelector, serverSelector);
		this.clientSelector = clientSelector;
		this.clientParser = new ClientParser(this);
	}

	@Override
	public void handleAccept(final SelectionKey key) throws IOException {
		final SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();

		String address = clientChannel.socket().getRemoteSocketAddress().toString();
		address = address.substring(1, address.indexOf(':'));
		LOGGER.info(address);

		// Connect to proxy service
		clientChannel.configureBlocking(false);
		clientChannel.socket().setKeepAlive(true);
		LOGGER.info("Accepted connection -> " + clientChannel.socket().getRemoteSocketAddress());

		final Connection connection = new Connection(clientChannel, clientSelector, this);
		connection.getClientBuffer().getWriteBuffer().append(POP3Command.CONNECTION.success());

		Statistics.INSTANCE.newClientRequest();

		clientChannel.register(clientSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, connection);
	}

	@Override
	public void handleRead(final SelectionKey key) throws IOException {
		final SocketChannel channel = (SocketChannel) key.channel();
		final Connection connection = ((Connection) key.attachment());
		final StringBuffer readBuffer = connection.getClientBuffer().getReadBuffer();
		final ByteBuffer buf = connection.getBuffer();

		try {
			final long bytesRead = channel.read(buf);

			buf.flip();

			if (bytesRead == -1) {
				LOGGER.info("Client disconnected:" + channel.socket().getRemoteSocketAddress());
				channel.close();
				connection.close();
			} else if (bytesRead > 0) {
				final String line = DoubleBuffer.bufferToString(buf);
				buf.clear();
				readBuffer.append(line);
				if (!line.endsWith("\n")) {
					return;
				}
				final String fullLine = readBuffer.toString().trim() + "\r\n";
				readBuffer.delete(0, readBuffer.length());

				clientParser.parse(fullLine, connection);
			}
		} catch (Exception e) {
			connection.close();
		}
	}

	@Override
	public void handleWrite(final SelectionKey key) throws IOException {
		final Connection connection = (Connection) key.attachment();
		final SocketChannel channel = connection.getClientChannel();
		final StringBuffer writeBuffer = connection.getClientBuffer().getWriteBuffer();
		final ByteBuffer buf = ByteBuffer.wrap(writeBuffer.toString().getBytes());

		try {
			final int bytesWritten = channel.write(buf);
			Statistics.INSTANCE.newTransfer(bytesWritten);

			LOGGER.debug("Proxy response -> " + writeBuffer.toString());

			if (!buf.hasRemaining()) {
				// Nothing left, so no longer interested in writes
				key.interestOps(SelectionKey.OP_READ);
			}
			writeBuffer.delete(0, bytesWritten);
			// Make room for more data to be read in
			buf.compact();
			buf.clear();
		} catch (Exception e){
			connection.close();
		}
	}
}
