package ar.edu.itba.pdc.parser;

import ar.edu.itba.pdc.connection.AdminConnection;
import ar.edu.itba.pdc.connection.Server;
import ar.edu.itba.pdc.statistics.Statistics;
import ar.edu.itba.pdc.utils.ProxyConfiguration;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminParser {

    private static final ProxyConfiguration CONFIGURATION = ProxyConfiguration.INSTANCE;
    private static final Statistics STATISTICS = Statistics.INSTANCE;
    private static final Logger LOGGER = Logger.getLogger(AdminParser.class);

    private static final String LIST_ENDING = ".\r\n";

    private static final EmailToServerMap EMAIL_TO_SERVER_MAP = EmailToServerMap.INSTANCE;

    public void parse(final String command, final AdminConnection adminConnection)
            throws IOException, InterruptedException {

        final List<String> commandSplit = Arrays.stream(command.split(" "))
                .map(String::trim).collect(Collectors.toList());
        final String keyword = commandSplit.get(0);
        final List<String> arguments = commandSplit.subList(1, commandSplit.size());
        AdminCommand adminCommand;

        try {
            adminCommand = AdminCommand.valueOf(keyword.toUpperCase());
        } catch (IllegalArgumentException e) {
            adminCommand = AdminCommand.UNKNOWN;
        }

        try {
            switch (adminCommand) {
                case AUTH:
                    doAuth(adminConnection, arguments, adminCommand);
                    break;
                case QUIT:
                    doQuit(adminConnection, adminCommand);
                    break;
                case STOP:
                    if (adminConnection.authenticated()) {
                        writeToChannel(adminConnection, adminCommand.success());
                        adminConnection.finishServer();
                    } else {
                        writeToChannel(adminConnection, authenticationNeededError());
                    }
                    break;
                case STATS:
                    if (adminConnection.authenticated()) {
                        writeStats(adminConnection, adminCommand);
                    } else {
                        writeToChannel(adminConnection, authenticationNeededError());
                    }
                    break;
                case LISTUSERS:
                    if (adminConnection.authenticated()) {
                        doListUsers(adminConnection, adminCommand);
                    } else {
                        writeToChannel(adminConnection, authenticationNeededError());
                    }
                    break;

                case SETSERVER:
                    if (adminConnection.authenticated()) {
                        doSetServer(adminConnection, arguments, adminCommand);
                    } else {
                        writeToChannel(adminConnection, authenticationNeededError());
                    }
                    break;

                case REMOVESERVER:
                    if (adminConnection.authenticated()) {
                        doRemoveUser(adminConnection, arguments, adminCommand);
                    } else {
                        writeToChannel(adminConnection, authenticationNeededError());
                    }
                    break;

                case LEET:
                    if (adminConnection.authenticated()) {
                        doSetLeet(adminConnection, arguments.get(0));
                    } else {
                        writeToChannel(adminConnection, authenticationNeededError());
                    }
                    break;

                case ROTATION:
                    if (adminConnection.authenticated()) {
                        doSetRotation(adminConnection, arguments.get(0));
                    } else {
                        writeToChannel(adminConnection, authenticationNeededError());
                    }
                    break;

                default:
                    writeToChannel(adminConnection, AdminCommand.UNKNOWN.error());
                    break;
            }
        } catch (IndexOutOfBoundsException e) {
            writeToChannel(adminConnection, AdminCommand.WRONG_PARAMETERS.error());
        }
    }

    private void doQuit(final AdminConnection adminConnection, final AdminCommand adminCommand)
            throws InterruptedException, IOException {
        writeToChannel(adminConnection, adminCommand.success());
        adminConnection.logOut();
        if (adminConnection.authenticated()) {
            LOGGER.info("ADMIN disconnected.");
        }
        adminConnection.close();
    }

    private void doAuth(final AdminConnection adminConnection, final List<String> arguments,
                        final AdminCommand adminCommand) throws InterruptedException, IOException {
        final String user = arguments.get(0);
        final String password = arguments.get(1);
        if (correctCredentials(user, password)) {
            adminConnection.authenticate();
            LOGGER.info("ADMIN: connected " + user);
            writeToChannel(adminConnection, adminCommand.success());
        } else {
            writeToChannel(adminConnection, adminCommand.error());
        }
    }

    private void writeStats(final AdminConnection adminConnection, final AdminCommand adminCommand)
            throws InterruptedException, IOException {
        writeToChannel(adminConnection, adminCommand.success());
        writeToChannel(adminConnection,
                "CLIENT_CONNECTIONS " + String.valueOf(STATISTICS.getClientRequests() + "\r\n"));
        writeToChannel(adminConnection,
                "BYTES_TRANSFERRED " + String.valueOf(STATISTICS.getBytesTransferred() + "\r\n"));
        writeToChannel(adminConnection, LIST_ENDING);
    }

    private void doSetLeet(final AdminConnection adminConnection, final String s)
            throws InterruptedException, IOException {
        final String operation = s.toUpperCase();
        switch (operation) {
            case "ON":
                ProxyConfiguration.INSTANCE.setLeetSubjectTransformation();
                writeToChannel(adminConnection, AdminCommand.LEET.success().trim() + " ON\r\n");
                break;
            case "OFF":
                ProxyConfiguration.INSTANCE.desactivateSubjectTransformation();
                writeToChannel(adminConnection, AdminCommand.LEET.success().trim() + " OFF\r\n");
                break;
            default:
                writeToChannel(adminConnection, AdminCommand.WRONG_PARAMETERS.error());
                break;
        }
    }

    private void doSetRotation(final AdminConnection adminConnection, final String s)
            throws InterruptedException, IOException {
        final String operation = s.toUpperCase();
        switch (operation) {
            case "ON":
                ProxyConfiguration.INSTANCE.setImageRotationTransformation();
                writeToChannel(adminConnection, AdminCommand.ROTATION.success().trim() + " ON\n");
                break;

            case "OFF":
                ProxyConfiguration.INSTANCE.desactivateImageTransformation();
                writeToChannel(adminConnection, AdminCommand.ROTATION.success().trim() + " OFF\n");
                break;

            default:
                writeToChannel(adminConnection, AdminCommand.WRONG_PARAMETERS.error());
                break;
        }
    }

    private void doSetServer(final AdminConnection adminConnection, final List<String> arguments,
                             final AdminCommand adminCommand) throws InterruptedException, IOException {
        final String email = arguments.get(0);
        final String host = arguments.get(1);
        final int port = Integer.valueOf(arguments.get(2));
        final Server server = new Server(host, port);
        EMAIL_TO_SERVER_MAP.addEmailToServerMapping(email, server);
        writeToChannel(adminConnection, adminCommand.success());
    }

    private void doRemoveUser(final AdminConnection adminConnection, final List<String> arguments,
                              final AdminCommand adminCommand) throws IOException, InterruptedException {
        final String email = arguments.get(0);
        final boolean success = EMAIL_TO_SERVER_MAP.removeEmailMapping(email);
        if (success) {
            writeToChannel(adminConnection, adminCommand.success());
        } else {
            writeToChannel(adminConnection, adminCommand.error());
        }
    }

    private void doListUsers(final AdminConnection adminConnection, final AdminCommand adminCommand)
            throws InterruptedException, IOException {
        writeToChannel(adminConnection, adminCommand.success());
        if (EMAIL_TO_SERVER_MAP.isEmpty()) {
            writeToChannel(adminConnection, "");
        } else {
            for (Map.Entry<String, Server> entry : EMAIL_TO_SERVER_MAP.getEntries()) {
                writeToChannel(adminConnection, entry.getKey() + ": " + entry.getValue().getName() + " " + Integer.valueOf(entry.getValue().getPort()).toString() + "\r\n");
            }
        }
        writeToChannel(adminConnection, LIST_ENDING);
    }

    private boolean correctCredentials(final String username, final String password) {
        final String pass = password.trim();
        LOGGER.info("ADMIN: attempted to connect " + username + pass);
        return CONFIGURATION.getUser().equals(username) && CONFIGURATION.getPassword().equals(pass);
    }

    private String authenticationNeededError() {
        return AdminCommand.AUTH_NEEDED.error();
    }

    private void writeToChannel(final AdminConnection connection, final String line)
            throws InterruptedException, IOException {
        final SelectionKey key = connection.getChannel().keyFor(connection.getSelector());
        connection.getBuffer().getWriteBuffer().append(line);
        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }
}
