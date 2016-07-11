package ar.edu.itba.pdc.core;

import ar.edu.itba.pdc.handler.AdminHandler;
import ar.edu.itba.pdc.handler.ClientHandler;
import ar.edu.itba.pdc.handler.Handler;
import ar.edu.itba.pdc.handler.ServerHandler;
import ar.edu.itba.pdc.utils.ProxyConfiguration;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.atomic.AtomicBoolean;

public class POP3Server {

    private static final ProxyConfiguration CONFIGURATION = ProxyConfiguration.INSTANCE;
    private static final Logger LOGGER = Logger.getLogger(POP3Server.class);

    private static String configPropertiesPath = null;

    public static void main(String[] args) throws IOException {

        if (args.length > 0 && !args[0].equals("")) {
            configPropertiesPath = args[0];
        } else {
            LOGGER.info("No properties path provided, using default properties.");
        }

        CONFIGURATION.init(configPropertiesPath);

        LOGGER.info("               User: " + CONFIGURATION.getUser());
        LOGGER.info("           Password: " + CONFIGURATION.getPassword());
        LOGGER.info("        Client port: " + CONFIGURATION.getClientPort());
        LOGGER.info("        Server port: " + CONFIGURATION.getServerPort());
        LOGGER.info("         Admin port: " + CONFIGURATION.getAdminPort());
        LOGGER.info("     Default server: " + CONFIGURATION.getKeyDefaultServer().getName());
        LOGGER.info("Default server port: " + Integer.toString(CONFIGURATION.getKeyDefaultServer().getPort()));
        LOGGER.info("         BufferSize: " + Integer.toString(CONFIGURATION.getBufferSize()) + " Bytes");
        LOGGER.info("   Pool Thread size: " + Integer.toString(CONFIGURATION.getThreadPoolSize()) + " threads");
        LOGGER.info("     Image max size: " + Integer.toString(CONFIGURATION.maxImageLengthMb()) + " MB");

        LOGGER.info("Starting server...");

        try {
            final Selector clientSelector = Selector.open();
            final Selector serverSelector = Selector.open();
            final Selector adminSelector = Selector.open();

            final AtomicBoolean finished = new AtomicBoolean(false);

            final Handler clientHandler = new ClientHandler(clientSelector, serverSelector);
            final Handler serverHandler = new ServerHandler(clientSelector, serverSelector);
            final Handler adminHandler = new AdminHandler(adminSelector, finished);

            final HandlerThread clientThread = new HandlerThread(clientSelector, clientHandler,
                                                           CONFIGURATION.getClientPort(), finished);
            final HandlerThread serverThread = new HandlerThread(serverSelector, serverHandler,
                                                           CONFIGURATION.getServerPort(), finished);
            final HandlerThread adminThread = new HandlerThread(adminSelector, adminHandler,
                                                          CONFIGURATION.getAdminPort(), finished);

            LOGGER.info("Starting client-side thread.");
            clientThread.start();
            LOGGER.info("Starting server-side thread.");
            serverThread.start();
            LOGGER.info("Starting Admin thread.");
            adminThread.start();

            try {
                adminThread.join();
                clientThread.join();
                serverThread.join();
                LOGGER.info("Server stopped.");
            } catch (InterruptedException e) {
                LOGGER.error("Something went wrong.");
            }

        } catch (IOException e) {
            LOGGER.error("Couldn't open selectors.");
            e.printStackTrace();
        }
    }
}
