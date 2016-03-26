package ar.edu.itba.pdc.tcp.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import ar.edu.itba.pdc.tcp.server.handlers.ConnectionHandler;
import ar.edu.itba.pdc.tcp.server.handlers.echo.EchoHandler;

public class ThreadPoolSocketServer  {
    private ServerSocket serverSocket;
    private ConnectionHandler handler;
    private int THREAD_POOL_SIZE = 10;

    public ThreadPoolSocketServer(final int port, final InetAddress interfaz, final ConnectionHandler handler)
            throws IOException {
        init(new ServerSocket(port, 50, interfaz), handler);
    }

    public ThreadPoolSocketServer(final int port, final ConnectionHandler handler) throws IOException {
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
        
        for(int i = 0; i < THREAD_POOL_SIZE; i++) {
            final int j=i;
            Thread thread = new Thread() {
                public void run() {
                    while(true) {
                        try {
                            Socket socket = ThreadPoolSocketServer.this.serverSocket.accept();
                            
                            String s = socket.getRemoteSocketAddress().toString();
                            System.out.printf("Se conecto %s a %d\n", s, j);
                            
                            ThreadPoolSocketServer.this.handler.handle(socket);
                            if (!socket.isClosed()) {
                                socket.close();
                                System.out.printf("Cerrando %s\n", s);
                            }
                            System.out.printf("Se desconecto %s\n", s);
                        } catch (IOException e) {
                            System.err.printf("Excepcion al manejar conexion\n");
                        }
                    }
                };
            };
            thread.start();
            System.out.println("Se inicio el thread " + thread.getName());
        }
    }

    public static void main(String[] args) {
        try {
            ThreadPoolSocketServer server = new ThreadPoolSocketServer(20007, InetAddress.getByName("localhost"), new EchoHandler());
            server.run();
        } catch (final Exception e) {
            System.out.println("Ocurrio un error");
            e.printStackTrace();
        }
    }

}
