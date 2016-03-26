package ar.edu.itba.pdc.tcp.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import ar.edu.itba.pdc.tcp.server.handlers.ConnectionHandler;
import ar.edu.itba.pdc.tcp.server.handlers.echo.EchoHandler;

public class ThreadedSocketServer  {
    private ServerSocket serverSocket;
    private ConnectionHandler handler;

    public ThreadedSocketServer(final int port, final InetAddress interfaz, final ConnectionHandler handler)
            throws IOException {
        init(new ServerSocket(port, 50, interfaz), handler);
    }

    public ThreadedSocketServer(final int port, final ConnectionHandler handler) throws IOException {
        init(new ServerSocket(port, 50), handler);
    }

    private void init(final ServerSocket s, final ConnectionHandler handler) {
    	if(s == null || handler == null) {
    		throw new IllegalArgumentException();
    	}

        this.serverSocket = s;
        this.handler = handler;
    }

    public void run() {
        System.out.printf("Escuchando en %s\n", serverSocket.getLocalSocketAddress());
        while (true) {
            final Socket socket;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            new Thread(new Runnable() {
                public void run() {
                    try {
                        String s = socket.getRemoteSocketAddress().toString();
                        System.out.printf("Se conecto %s\n", s);
                        ThreadedSocketServer.this.handler.handle(socket);
                        if (!socket.isClosed()) {
                            socket.close();
                            System.out.printf("Cerrando %s\n", s);
                        }
                        System.out.printf("Se desconecto %s\n", s);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();

        }

    }

    public static void main(String[] args) {
        try {
            ThreadedSocketServer server = new ThreadedSocketServer(20007, InetAddress.getByName("localhost"), new EchoHandler());
            server.run();
        } catch (final Exception e) {
            System.out.println("Ocurrio un error");
            e.printStackTrace();
        }
    }

}
