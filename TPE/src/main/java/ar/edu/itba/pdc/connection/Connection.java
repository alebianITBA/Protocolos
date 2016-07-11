package ar.edu.itba.pdc.connection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

import ar.edu.itba.pdc.handler.ProxyHandler;
import ar.edu.itba.pdc.mail.MimeParser;
import ar.edu.itba.pdc.transformations.Transformation;
import org.apache.log4j.Logger;

import ar.edu.itba.pdc.parser.POP3Command;
import ar.edu.itba.pdc.utils.ProxyConfiguration;

public class Connection {

    private static final Logger LOGGER = Logger.getLogger(Connection.class);
    private static final ProxyConfiguration CONFIGURATION = ProxyConfiguration.INSTANCE;

    private final Selector selector;
    private final SocketChannel clientChannel;

    private final ByteBuffer buffer;
    private final DoubleBuffer clientBuffer;
    private final DoubleBuffer serverBuffer;
    private final AtomicBoolean serverResponded; // Used to ignore the server's greeting
    private final MimeParser mimeParser;

    private SocketChannel serverChannel;
    private State state;
    private POP3Command lastPOP3Command;

    private Transformation imageTransformation;
    private Transformation subjectTransformation;

    private final Stack<String> boundaryStack;
    private boolean inBody;

    public Connection(final SocketChannel clientChannel, final Selector selector, final ProxyHandler handler) {
        this.selector = selector;
        this.clientChannel = clientChannel;
        this.state = State.AUTHORIZATION_USER;
        this.clientBuffer = new DoubleBuffer(CONFIGURATION.getBufferSize());
        this.buffer = ByteBuffer.allocate(CONFIGURATION.getBufferSize());
        this.lastPOP3Command = POP3Command.UNKNOWN;
        this.serverResponded = new AtomicBoolean(false);
        this.mimeParser = new MimeParser(handler, this);
        this.serverBuffer = new DoubleBuffer(CONFIGURATION.getBufferSize());
        this.boundaryStack = new Stack<>();
        this.inBody = false;
    }

    public SocketChannel getServerChannel() {
        return serverChannel;
    }

    public SocketChannel getClientChannel() {
        return clientChannel;
    }

    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }

    public void connectToServer(final SocketChannel serverChannel) {
        this.serverChannel = serverChannel;
    }

    public void setLastPOP3Command(final POP3Command lastPOP3Command) {
        this.lastPOP3Command = lastPOP3Command;
    }

    public POP3Command getLastPOP3Command() {
        return lastPOP3Command;
    }

    public Stack<String> getBoundaryStack() {
        return boundaryStack;
    }

    public boolean getInBody() {
        return inBody;
    }

    public void close() {
        try {
            SelectionKey clientKey = clientChannel.keyFor(selector);
            if (clientKey != null && clientKey.isValid()) {
                clientKey.cancel();
            }
            clientChannel.close();
            if (serverChannel != null) {
                SelectionKey serverKey = serverChannel.keyFor(selector);
                if (serverKey != null && serverKey.isValid()) {
                    serverKey.cancel();
                }
                serverChannel.close();
            }
        } catch (IOException e) {
            LOGGER.error("Error closing socket");
        }
    }

    public DoubleBuffer getClientBuffer() {
        return clientBuffer;
    }

    public DoubleBuffer getServerBuffer() {
        return serverBuffer;
    }

    public boolean serverResponded() {
        return serverResponded.get();
    }

    public void setServerResponded() {
        this.serverResponded.set(true);
    }

    public void setInBody(final boolean inBody) {
        this.inBody = inBody;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
    
    public MimeParser getMimeParser() {
        return mimeParser;
    }

    public void prepareForNextMail() {
        if (this.mimeParser != null) {
            this.mimeParser.reset();
        }
        this.imageTransformation = null;
        this.subjectTransformation = null;
    }

    public void setTransformations() {
        if(this.imageTransformation == null) {
            this.imageTransformation = CONFIGURATION.imageTransformation();
        }
        if(this.subjectTransformation == null) {
            this.subjectTransformation = CONFIGURATION.subjectTransformation();
        }
    }

    public Transformation getImageTransformation() {
        return this.imageTransformation;
    }

    public Transformation getSubjectTransformation() {
        return this.subjectTransformation;
    }

}
