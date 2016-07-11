package ar.edu.itba.pdc.parser;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;

import ar.edu.itba.pdc.connection.Connection;
import ar.edu.itba.pdc.connection.Server;

public abstract class Parser {
    protected static final String
            OK = "+OK",
            ERR = "-ERR",
            END = ".";
    public abstract void parse(final String commandLine, final Connection connection) throws IOException;
}
