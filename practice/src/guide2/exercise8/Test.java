package guide2.exercise8;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;

/**
 * Created by alebezdjian on 24/3/16.
 */
public class Test {
    public static void main(String[] args) throws IOException {
        URL url = new URL("http://www.google.com.ar");
        Socket socket = new Socket(url.getHost(), 80);

        Downloader.getResource(url.getHost(), url.getPath(), socket);

        socket.close();
    }
}
