/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class BufferStream {

    public static class ByteBufferBackedInputStream extends InputStream {

        ByteBuffer buf;

        public ByteBufferBackedInputStream(ByteBuffer buf) {
            this.buf = buf;
        }

        public int read() throws IOException {
            if (!buf.hasRemaining()) {
                return -1;
            }
            return buf.get() & 0xFF;
        }

        public int read(byte[] bytes, int off, int len)
                throws IOException {
            if (!buf.hasRemaining()) {
                return -1;
            }

            len = Math.min(len, buf.remaining());
            buf.get(bytes, off, len);
            return len;
        }
    }

    public static class ByteBufferBackedOutputStream extends OutputStream {

        ByteBuffer buf;

        public ByteBufferBackedOutputStream(ByteBuffer buf) {
            this.buf = buf;
        }

        public void write(int b) throws IOException {
            buf.put((byte) b);
        }

        public void write(byte[] bytes, int off, int len)
                throws IOException {
            buf.put(bytes, off, len);
        }

    }
}
