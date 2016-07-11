package ar.edu.itba.pdc.parser;

import ar.edu.itba.pdc.connection.Connection;
import ar.edu.itba.pdc.connection.State;
import ar.edu.itba.pdc.handler.ProxyHandler;

import java.io.IOException;

public class ServerParser extends Parser {

    private final ProxyHandler handler;

    public ServerParser(final ProxyHandler handler) {
        this.handler = handler;
    }

    @Override
    public void parse(String commandLine, final Connection connection) throws IOException {

        switch (connection.getLastPOP3Command()) {
            case USER:
                if (connection.serverResponded()) {
                    handler.writeToClient(commandLine, connection);
                } else {
                    connection.setServerResponded();
                }
                break;

            case PASS:
                if (commandLine.startsWith(OK)) {
                    connection.setState(State.TRANSACTION);
                } else if (commandLine.startsWith(ERR)) {
                    connection.setState(State.AUTHORIZATION_USER);
                }
                handler.writeToClient(commandLine, connection);
                break;

            case LIST:
                handler.writeToClient(commandLine, connection);
                break;

            case RETR:
                if (commandLine.startsWith(ERR)) {
                    handler.writeToClient(commandLine, connection);
                } else {
                    try {
                        connection.setTransformations();
                        String answer = connection.getMimeParser().parse(commandLine);
                        handler.writeToClient(answer, connection);
                    } catch (Exception e) {
                        handler.writeToClient(POP3Command.CONNECTION.error(), connection);
                    }
                }
                break;

            case DELE:
                handler.writeToClient(commandLine, connection);
                break;

            case QUIT:
                if (connection.getState() == State.TRANSACTION) {
                    connection.setState(State.UPDATE);
                }
                handler.writeToClient(commandLine, connection);
                connection.close();
                break;

            case STAT:
                handler.writeToClient(commandLine, connection);
                break;

            case NOOP:
                handler.writeToClient(commandLine, connection);
                break;

            case RSET:
                handler.writeToClient(commandLine, connection);
                break;

            case TOP:
                handler.writeToClient(commandLine, connection);
                break;

            case APOP:
                handler.writeToClient(commandLine, connection);
                break;

            case UIDL:
                handler.writeToClient(commandLine, connection);
                break;

            default:
                break;
        }
    }

}
