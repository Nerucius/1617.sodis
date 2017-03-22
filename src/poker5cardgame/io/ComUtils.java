package poker5cardgame.io;

import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import poker5cardgame.network.Network;
import poker5cardgame.network.Network.Command;
import poker5cardgame.network.Packet;

import static poker5cardgame.Log.*;

public class ComUtils {

    // TODO @alex Test Socket Timeout

    /* Mida d'una cadena de caracters */
    private final int STRSIZE = 32;

    /* Objectes per escriure i llegir dades */
    private DataInputStream dis;
    //private DataOutputStream dos;
    //private BufferedInputStream bis;
    private BufferedOutputStream bos;

    // Network
    protected Socket socket;
    int maxTimeout = 5;
    int timeOutMillis = 2000;

    // Private State
    private int expectedCards = 0;

    public ComUtils(Socket socket) {

        try {
            this.socket = socket;
            dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            // Set one Second timeout
            this.socket.setSoTimeout(timeOutMillis);

            // Buffered versions -> Only 1 TCP packet per command.
            //bis = new BufferedInputStream(dis);
            bos = new BufferedOutputStream(dos);
        } catch (IOException ex) {
            IO_ERROR("CU: Failed to Open Socket");
        }
    }

    public Socket getSocket() {
        return socket;
    }

    /**
     * Packet Reading wrapper for real reading function. Deals with timeouts.
     */
    public Packet read_NetworkPacket() {
        IO_TRACE("CU: Waiting for next Packet");

        int toCount = 0;
        Packet packet = null;

        while (true) {

            try {
                // Try to read next packet
                packet = _read_NetworkPacket();
                return packet;

            } catch (SocketTimeoutException e) {
                // Timing out allowed for a number of tries
                toCount++;
                IO_ERROR("CU: Timed out " + toCount + " times.");

                // Upon reaching max timeout times, stop trying to read
                if (toCount >= maxTimeout) {
                    NET_ERROR("CU: Max timeout reached. Disconnecting");
                    send_error_packet("Max Timeout Reached.");

                    packet = new Packet(Command.NET_ERROR);
                    return packet;
                }
            }
        }

    }

    /**
     * Reads the next Network Packet received by the server and returns it for
     * further processing.
     *
     * @return received Network Packet.
     */
    private Packet _read_NetworkPacket() throws SocketTimeoutException {
        Packet packet = new Packet(null);

        try {
            // Read first 4 bytes (4 chars) to identify code
            byte[] nextBytes = read_bytes(4);

            // While the current 4 bytes are not a valid command, read one more byte
            while (!Network.Command.isValid(new String(nextBytes))) {
                // Log invalid packet code
                IO_TRACE("CU: Invalid packet: " + new String(nextBytes));

                // Send an error packet every time this fails
                send_error_packet("Invalid PROTOCOL Code");
                // Move last 3 bytes back, and read one more
                System.arraycopy(nextBytes, 1, nextBytes, 0, 3);
                nextBytes[3] = read_bytes(1)[0];
            }

            String opCode = new String(nextBytes);
            opCode = opCode.toUpperCase();

            packet.command = Network.Command.identifyCode(opCode);
            IO_TRACE("CU: detected packet: " + packet.command);
            // Read arguments from Stream if applicable
            if (Packet.hasArgs(packet))
                read_PacketArgs(packet);

            IO_TRACE("CU: Received: " + packet);

        } catch (SocketTimeoutException e) {
            // TODO do something with exception ? see current read bytes
            throw e;
        } catch (IOException e) {
            IO_ERROR("CU: Socket Error (closed?)");
            packet.command = Command.NET_ERROR;
        }

        return packet;
    }

    /**
     * Returns true if could be sent. False if there was a problem
     *
     * @param packet Packet to send over the net
     * @return
     */
    public boolean write_NetworkPacket(Packet packet) {
        try {
            // Packet knows how to write itself. yay
            packet.write(this);
            bos.flush();
            IO_TRACE("CU: Sent: " + packet);

            // Intercept DRAW message to get expected Cards
            if (packet.command == Command.DRAW) {
                // Now reading a 'X' string
                expectedCards = Integer.valueOf(packet.getField("number", String.class));
            }

            return true;

        } catch (IOException e) {
            IO_ERROR("CU: Error sending Packet");
            return false;
        }
    }

    private void read_PacketArgs(Packet packet) throws IOException {
        read_bytes(1); // Consume Space

        // TODO @alex Implement reading packet args for every command type
        switch (packet.command) {
            case START:
                packet.putField("id", read_int32());
                break;
            case ANTE:
                packet.putField("chips", read_int32());
                break;
            case STAKES:
                packet.putField("stakes_client", read_int32());
                read_bytes(1);
                packet.putField("stakes_server", read_int32());
                break;
            case DEALER:
                int dealer = read_byte_as_int();
                packet.putField("dealer", dealer);
                break;
            case HAND:
                packet.putField("cards", read_cards(5));
                break;
            case BET:
                packet.putField("chips", read_int32());
                break;
            case RAISE:
                packet.putField("chips", read_int32());
                break;
            case DRAW:
                int drawCount = read_byte_as_int();
                packet.putField("number", drawCount);
                if (drawCount > 0) {
                    read_bytes(1); // Consume space
                    packet.putField("cards", read_cards(drawCount));
                }
                break;
            case DRAW_SERVER:
                if (expectedCards > 0) {
                    packet.putField("cards", read_cards(expectedCards));
                    read_bytes(1); // Consume space
                }
                packet.putField("number", read_byte_as_int());
                break;
            case SHOWDOWN:
                packet.putField("cards", read_cards(5));
                break;
            case ERROR:
                packet.putField("error", read_string_variable(2));
                break;

        }
    }

    private void send_error_packet(String str) {
        Packet p = new Packet(Command.ERROR);
        p.putWrittable(new Writable.VariableString(2, str));
        write_NetworkPacket(p);
    }

    private int read_byte_as_int() throws IOException {
        return Integer.valueOf(new String(read_bytes(1)));
    }

    /* Llegir un enter de 32 bits */
    public int read_int32() throws IOException {
        return bytesToInt32(read_bytes(4), "be");
    }

    /* Escriure un enter de 32 bits */
    public void write_int32(int number) throws IOException {
        byte bytes[] = new byte[4];

        int32ToBytes(number, bytes, "be");
        bos.write(bytes, 0, 4);
        //dos.write(bytes, 0, 4);
    }

    /* Llegir un string de mida STRSIZE */
    public String read_string() throws IOException {
        String str;
        byte bStr[] = new byte[STRSIZE];
        char cStr[] = new char[STRSIZE];

        bStr = read_bytes(STRSIZE);

        for (int i = 0; i < STRSIZE; i++)
            cStr[i] = (char) bStr[i];

        str = String.valueOf(cStr);

        return str.trim();
    }

    /**
     * Read a specified number of chars (1 byte each) from the stream
     */
    public String read_chars(int num) throws IOException {
        byte[] chars = new byte[4];
        dis.read(chars, 0, num);
        return new String(chars);
    }

    /* Escriure un string */
    public void write_string(String str) throws IOException {
        int numBytes, lenStr;
        byte bStr[] = new byte[STRSIZE];

        lenStr = str.length();

        if (lenStr > STRSIZE)
            numBytes = STRSIZE;
        else
            numBytes = lenStr;

        for (int i = 0; i < numBytes; i++)
            bStr[i] = (byte) str.charAt(i);

        for (int i = numBytes; i < STRSIZE; i++)
            bStr[i] = (byte) ' ';

        bos.write(bStr, 0, STRSIZE);
    }

    public void write_string_pure(String str) throws IOException {
        bos.write(str.getBytes(), 0, str.length());
    }

    /* Passar d'enters a bytes */
    private int int32ToBytes(int number, byte bytes[], String endianess) {
        if ("be".equals(endianess.toLowerCase())) {
            bytes[0] = (byte) ((number >> 24) & 0xFF);
            bytes[1] = (byte) ((number >> 16) & 0xFF);
            bytes[2] = (byte) ((number >> 8) & 0xFF);
            bytes[3] = (byte) (number & 0xFF);
        } else {
            bytes[0] = (byte) (number & 0xFF);
            bytes[1] = (byte) ((number >> 8) & 0xFF);
            bytes[2] = (byte) ((number >> 16) & 0xFF);
            bytes[3] = (byte) ((number >> 24) & 0xFF);
        }
        return 4;
    }

    /* Passar de bytes a enters */
    private int bytesToInt32(byte bytes[], String endianess) {
        int number;

        if ("be".equals(endianess.toLowerCase())) {
            number = ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16)
                    | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
        } else {
            number = (bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8)
                    | ((bytes[2] & 0xFF) << 16) | ((bytes[3] & 0xFF) << 24);
        }
        return number;
    }
    //llegir bytes.

    private byte[] read_bytes(int numBytes) throws IOException {
        int len = 0;
        byte bStr[] = new byte[numBytes];
        int bytesread = 0;
        do {
            bytesread = dis.read(bStr, len, numBytes - len);
            if (bytesread == -1) {
                throw new IOException("Broken Pipe");
            }
            len += bytesread;
        } while (len < numBytes);
        return bStr;
    }

    public void flushBuffer() throws IOException {
        bos.flush();
    }

    /* Llegir un string  mida variable size = nombre de bytes especifica la longitud*/
    public String read_string_variable(int size) throws IOException {
        byte bHeader[] = new byte[size];
        char cHeader[] = new char[size];
        int numBytes = 0;

        // Llegim els bytes que indiquen la mida de l'string
        bHeader = read_bytes(size);
        // La mida de l'string ve en format text, per tant creem un string i el parsejem
        for (int i = 0; i < size; i++) {
            cHeader[i] = (char) bHeader[i];
        }
        numBytes = Integer.parseInt(new String(cHeader));

        // Llegim l'string
        byte bStr[] = new byte[numBytes];
        char cStr[] = new char[numBytes];
        bStr = read_bytes(numBytes);
        for (int i = 0; i < numBytes; i++)
            cStr[i] = (char) bStr[i];
        return String.valueOf(cStr);
    }

    /* Escriure un string mida variable, size = nombre de bytes especifica la longitud  */
 /* String str = string a escriure.*/
    public void write_string_variable(int size, String str) throws IOException {

        // Creem una seqüència amb la mida
        byte bHeader[] = new byte[size];
        String strHeader;
        int numBytes = 0;

        // Creem la capçalera amb el nombre de bytes que codifiquen la mida
        numBytes = str.length();

        strHeader = String.valueOf(numBytes);
        int len;
        if ((len = strHeader.length()) < size)
            for (int i = len; i < size; i++) {
                strHeader = "0" + strHeader;
            }
        for (int i = 0; i < size; i++)
            bHeader[i] = (byte) strHeader.charAt(i);
        // Enviem la capçalera
        bos.write(bHeader, 0, size);
        // Enviem l'string writeBytes de DataOutputStrem no envia el byte més alt dels chars.
        write_string_pure(str);
    }

    /**
     * Reads a stream of card codes and returns a Card[] interpretation.
     * Automatically consumes spaces in between cards.
     *
     * @param num Number of cards to read
     * @return
     * @throws IOException
     */
    private String read_cards(int num) throws IOException {
        String[] cards = new String[num];

        for (int i = 0; i < num; i++) {
            if (i != 0)
                read_bytes(1); // Consume space after the first card
            char[] cs = new char[3];
            cs[0] = (char) read_bytes(1)[0];
            cs[1] = (char) read_bytes(1)[0];

            if (cs[0] == '1') {
                // Read last character for 10X cards
                cs[2] = (char) read_bytes(1)[0];
                cards[i] = String.valueOf(cs);
            } else {
                // Cut array to 2 places otherwise
                cards[i] = String.valueOf(Arrays.copyOf(cs, 2));
            }

        }
        return String.join(" ", cards);
    }

    /**
     * Set the socket timeout time, and Max number of timeouts // TODO @alex add
     * max timeout count argument
     *
     * @param timeout millisecods
     */
    public void setTimeout(int timeout) {
        if (socket == null || socket.isClosed()) {
            IO_ERROR("CU: Can't set timeout with no connection.");
            return;
        }

        try {
            socket.setSoTimeout(timeout);
        } catch (SocketException ex) {
            Logger.getLogger(ComUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
