package ar.edu.itba.pdc.connection;

public class Server {

    private final String name;
    private final int port;

    public Server(final String name, final int port){
        this.name = name;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

}
