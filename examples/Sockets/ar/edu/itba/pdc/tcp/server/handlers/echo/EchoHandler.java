package ar.edu.itba.pdc.tcp.server.handlers.echo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import ar.edu.itba.pdc.tcp.server.handlers.ConnectionHandler;

/**
 * Connection Handler que implementa protocolo ECHO.  
 * 
 * 
 * @author Fernando Zunino
 * @since Aug 21, 2011
 */
public class EchoHandler implements ConnectionHandler {
    
    private static final int BUFSIZE = 32;   // Size of receive buffer

    /** @see ar.edu.itba.pdc.tcp.server.handlers.ConnectionHandler#handle(java.net.Socket) */
    @Override
    public void handle(final Socket s) throws IOException {
        InputStream in = s.getInputStream();
        OutputStream out = s.getOutputStream();
        
        int recvMsgSize;   // Size of received message
        byte[] receiveBuf = new byte[BUFSIZE];  // Receive buffer

        // Receive until client closes connection, indicated by -1 return
        while ((recvMsgSize = in.read(receiveBuf)) != -1) {
          out.write(receiveBuf, 0, recvMsgSize);
        }
        
        s.close();  // Close the socket.  We are done with this client!
    }
}
