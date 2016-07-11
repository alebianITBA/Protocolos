package ar.edu.itba.pdc.connection;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;

public class DoubleBuffer {

    private final StringBuffer readBuffer;
    private final StringBuffer writeBuffer;

    public DoubleBuffer(final int size) {
        readBuffer = new StringBuffer(size);
        writeBuffer = new StringBuffer(size);
    }

    public StringBuffer getReadBuffer() {
        return readBuffer;
    }

    public StringBuffer getWriteBuffer() {
        return writeBuffer;
    }

    public static String bufferToString(final ByteBuffer buffer) throws CharacterCodingException {
        return new String(buffer.array(), 0, buffer.remaining());
    }
}
