package guide2.exercise9;

import guide2.exercise8.Downloader;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alebezdjian on 24/3/16.
 */
public class Test {
    public static void main(String[] args) throws IOException {
        List<URL> urls = new ArrayList<URL>();
        urls.add(new URL("http://www.google.com"));
        urls.add(new URL("http://www.google.com/asd"));
        urls.add(new URL("http://www.google.com/asd/asd"));

        Socket socket = new Socket(urls.get(0).getHost(), 80);

        for (URL url: urls) {
            Downloader.getResourceWithLimit(url.getHost(), url.getPath(), socket, 100);
        }

        socket.close();
    }
}
