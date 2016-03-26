package guide2.exercise11;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

/**
 * Created by alebezdjian on 26/3/16.
 */
public class KeyValueStoreServer {

    private ServerSocket serverSocket;
    private ConcurrentMap<String, Integer> map;
    private Map<String, Integer> commands;
    private final Semaphore lock = new Semaphore(1, true);
    private static final int BUFFER_SIZE = 32;

    public KeyValueStoreServer(final int port, InetAddress address) throws IOException {
        this.serverSocket = new ServerSocket(port, 50, address);
        this.map = new ConcurrentHashMap<String, Integer>();
        this.commands = new HashMap<String, Integer>();
        loadCommands();
    }

    public void run(){
        System.out.printf("Listening in %s\n", serverSocket.getLocalSocketAddress());
        while (true) {
            final Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            new Thread(new Runnable() {
                public void run() {
                    try {
                        String s = clientSocket.getRemoteSocketAddress().toString();
                        System.out.printf("Connected %s\n", s);

                        InputStream in = clientSocket.getInputStream();
                        OutputStream out = clientSocket.getOutputStream();

                        int receivedMessageSize;   // Size of received message
                        byte[] receiveBuf = new byte[BUFFER_SIZE];  // Receive buffer


                        while ((receivedMessageSize = in.read(receiveBuf)) != -1) {
                            if(receiveBuf[receivedMessageSize] == 0) {
                                break;
                            }
                        }

                        System.out.println("READ: " + new String(receiveBuf));

                        String response = analyzeRequest(receiveBuf, receivedMessageSize);

                        System.out.println("RESPONSE: " + response);

                        out.write(response.getBytes());

                        if (!clientSocket.isClosed()) {
                            clientSocket.close();
                            System.out.printf("Closing connection with %s\n", s);
                        }
                        System.out.printf("Disconnected: %s\n", s);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
    }

    private void loadCommands() {
        commands.put("INC", 1);
        commands.put("DEC", 2);
        commands.put("GET", 3);
        commands.put("SET", 4);
    }

    private String analyzeRequest(byte[] data, int size) throws InterruptedException {
        String receivedCommand = new String(data, 0, size);
        String[] parts = receivedCommand.split("-");

        if (!commands.containsKey(parts[0])) {
            Set<String> availableCommands = commands.keySet();
            StringBuilder response = new StringBuilder("INVALID COMMAND. COMMANDS AVAILABLE: ");
            for (String s :availableCommands) {
                response.append(s);
                response.append(" ");
            }
            return response.toString();
        }

        switch (commands.get(parts[0])) {
            case 1:
                increment(parts[1], Integer.valueOf(parts[2]));
                break;
            case 2:
                decrement(parts[1], Integer.valueOf(parts[2]));
                break;
            case 3:
                return get(parts[1]).toString();
            case 4:
                set(parts[1], Integer.valueOf(parts[2]));
                break;
        }
        return "OK";
    }

    private void increment(String key, int amount) throws InterruptedException {
        lock.acquire();
        if (map.containsKey(key)){
            Integer oldValue = map.get(key);
            Integer newValue = oldValue + amount;
            map.put(key, newValue);
            System.out.println("INCREMENTED KEY: " + key + " FROM: " + oldValue.toString() + " TO: " + newValue.toString());
        }
        lock.release();
    }

    private void decrement(String key, int amount) throws InterruptedException {
        lock.acquire();
        if (map.containsKey(key)){
            Integer oldValue = map.get(key);
            Integer newValue = oldValue - amount;
            map.put(key, newValue);
            System.out.println("DECREMENTED KEY: " + key + " FROM: " + oldValue.toString() + " TO: " + newValue.toString());
        }
        lock.release();
    }

    private void set(String key, Integer value) throws InterruptedException {
        lock.acquire();
        map.put(key, value);
        System.out.println("SET KEY: " + key + " TO: " + value.toString());
        lock.release();
    }

    private Integer get(String key) throws InterruptedException {
        Integer value = map.get(key);
        System.out.println("RETURN KEY: " + key + " VALUE: " + value.toString());
        return value;
    }

    public static void main(String[] args) {
        try {
            KeyValueStoreServer server = new KeyValueStoreServer(20007, InetAddress.getByName("localhost"));
            server.run();
        } catch (final Exception e) {
            System.out.println("Something went wrong");
            e.printStackTrace();
        }
    }
}
