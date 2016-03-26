package guide2.exercise8;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;

/**
 * Created by alebezdjian on 24/3/16.
 */
public class Downloader {
    public static void getResource(final String host, final String path, final Socket socket) throws IOException {
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        String toWrite = "GET /" + path + " HTTP/1.1\nhost:" + host + "\n\n";
        out.write(toWrite.getBytes());

        int data;
        char[] character;
        char[] lastCharacter = new char[1];
        while (true) {
            data = in.read();
            character = Character.toChars(data);
            if (finishRead(data, character, lastCharacter)) break;
            lastCharacter = character;
            System.out.print(Character.toChars(data));
        }
    }

    public static void getResourceWithLimit(final String host, final String path, final Socket socket, int length) throws IOException {
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        String toWrite = "GET /" + path + " HTTP/1.1\nhost:" + host + "\n\n";
        out.write(toWrite.getBytes());

        byte[] data = new byte[1];
        int read = 0;
        while (read < length) {
            in.read(data, 0, 1);
            System.out.print(new String(data, "UTF-8"));
            read++;
        }
    }

    private static boolean finishRead(int data, char[] character, char[] lastCharacter) {
        if (data == -1 || data == 0) {
            return true;
        }
        if (String.valueOf(character).equals("0") && String.valueOf(lastCharacter).equals("\n")) {
            return true;
        }
        return false;
    }
}
