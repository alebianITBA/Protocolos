package ar.edu.itba.pdc.parser;

import ar.edu.itba.pdc.connection.Connection;
import ar.edu.itba.pdc.connection.Server;
import ar.edu.itba.pdc.connection.State;
import ar.edu.itba.pdc.handler.ProxyHandler;
import ar.edu.itba.pdc.utils.ProxyConfiguration;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClientParser extends Parser {

    private static final ProxyConfiguration CONFIGURATION = ProxyConfiguration.INSTANCE;
    private static final Logger LOGGER = Logger.getLogger(ClientParser.class);
    private static final EmailToServerMap EMAIL_TO_SERVER_MAP = EmailToServerMap.INSTANCE;

    private final ProxyHandler handler;

    public ClientParser(final ProxyHandler handler) {
        this.handler = handler;
    }

    @Override
    public void parse(final String command, final Connection connection) throws IOException {
        final List<String> commandSplit = Arrays.stream(command.split(" "))
                .map(String::trim).collect(Collectors.toList());
        final String keyword = commandSplit.get(0);
        final List<String> arguments = commandSplit.subList(1, commandSplit.size());

        POP3Command clientCommand;

        try {
            clientCommand = POP3Command.valueOf(keyword.toUpperCase());
        } catch (IllegalArgumentException e) {
            clientCommand = POP3Command.UNKNOWN;
        }

        LOGGER.info("Client's request -> " + command.trim() + " from " + connection.getClientChannel().getRemoteAddress().toString());

        switch (clientCommand) {
            case USER:
                if (arguments.size() != 1) {
                    handler.writeToClient(POP3Command.UNKNOWN.error(), connection);
                    return;
                }
                try {
                    handleUser(connection, arguments);
                    handler.writeToServer(command, connection);
                } catch (Exception e) {
                    handler.writeToClient(POP3Command.CONNECTION.error(), connection);
                }
                break;

            case PASS:
                if (connection.getState() != State.AUTHORIZATION_PASS ||
                        connection.getLastPOP3Command() != POP3Command.USER ||
                        arguments.size() != 1) {
                    handler.writeToClient(POP3Command.UNKNOWN.error(), connection);
                    return;
                }
                handler.writeToServer(command, connection);
                connection.setLastPOP3Command(clientCommand);
                break;

            case LIST:
                if (connection.getState() != State.TRANSACTION || arguments.size() > 2) {
                    handler.writeToClient(POP3Command.UNKNOWN.error(), connection);
                    return;
                }
                LOGGER.info("Client requested emails list.");
                handler.writeToServer(command, connection);
                connection.setLastPOP3Command(clientCommand);
                break;


            case RETR:
                if (connection.getState() != State.TRANSACTION || arguments.size() != 1) {
                    handler.writeToClient(POP3Command.UNKNOWN.error(), connection);
                    return;
                }
                LOGGER.info("Retrieving Email: " + arguments.get(0));
                handler.writeToServer(command, connection);
                connection.setLastPOP3Command(clientCommand);
                break;

            case DELE:
                if (connection.getState() != State.TRANSACTION || arguments.size() != 1) {
                    handler.writeToClient(POP3Command.UNKNOWN.error(), connection);
                    return;
                }
                handler.writeToServer(command, connection);
                LOGGER.info("Deleting Email: " + arguments.get(0));
                connection.setLastPOP3Command(clientCommand);
                break;

            case QUIT:
                if (arguments.size() != 0) {
                    handler.writeToClient(POP3Command.UNKNOWN.error(), connection);
                    return;
                }
                if (connection.getState() == State.AUTHORIZATION_USER) {
                    handler.writeToClient(POP3Command.QUIT.success(), connection);
                    connection.close();
                } else {
                    handler.writeToServer(command, connection);
                    connection.setLastPOP3Command(clientCommand);
                }
                break;

            case STAT:
            case NOOP:
            case RSET:
            case APOP:
            case TOP:
            case UIDL:
                // Default behaviour
                if (connection.getState() != State.TRANSACTION) {
                    handler.writeToClient(POP3Command.UNKNOWN.error(), connection);
                    return;
                }
                handler.writeToServer(command, connection);
                connection.setLastPOP3Command(clientCommand);
                break;
            case CAPA:
                handler.writeToClient(clientCommand.success() + "USER\r\n" + POP3Command.CRLF, connection);
                break;

            default:
                handler.writeToClient(clientCommand.error(), connection);
                break;
        }
    }

    private void handleUser(final Connection connection, final List<String> arguments) throws IOException {
        Server server = EMAIL_TO_SERVER_MAP.getServer(arguments.get(0));
        if (server == null) {
            server = CONFIGURATION.getKeyDefaultServer();
        }
        handler.connectToServer(connection, server);
        connection.setLastPOP3Command(POP3Command.USER);
        connection.setState(State.AUTHORIZATION_PASS);
    }
}
