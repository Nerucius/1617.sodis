package poker5cardgame.io;

import java.net.*;
import java.io.*;
import java.util.Arrays;
import poker5cardgame.game.Card;
import poker5cardgame.network.Network;
import poker5cardgame.network.Network.Command;
import poker5cardgame.network.Packet;

public class ComUtils {

    /* Mida d'una cadena de caracters */
    private final int STRSIZE = 32;

    /* Objectes per escriure i llegir dades */
    private DataInputStream dis;
    private DataOutputStream dos;
    //private BufferedInputStream bis;
    private BufferedOutputStream bos;
    protected Socket socket;

    public ComUtils(Socket socket) {
        try {
            this.socket = socket;
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            // Buffered versions -> Only 1 TCP packet per command. More stable reads
            //bis = new BufferedInputStream(dis);
            bos = new BufferedOutputStream(dos);
        } catch (IOException ex) {
            System.err.println("COM: Failed to Open Socket");
        }
    }

    public Socket getSocket() {
        return socket;
    }

    /**
     * Reads the next Network Packet received by the server and returns it for
     * further processing.
     *
     * @return received Network Packet.
     */
    public Packet read_NetworkPacket() {
        Packet packet = new Packet(null);

        try {
            // Read first 4 bytes (4 chars) to identify code
            String opcode = read_chars(4);
            packet.command = Network.Command.identifyPacket(opcode);
            System.out.println("COM: detected packet: "+packet.command);
            // Read arguments from Stream if applicable
            if (Packet.hasArgs(packet))
                read_PacketArgs(packet);

        } catch (IOException e) {
            System.err.println("CU: Error Reading socket");
            packet.command = Command.NET_ERROR;

        } catch (IllegalArgumentException e) {
            System.err.println("CU: Malformed PROTOCOL code");
            packet.command = Command.ERROR;
            packet.putField("error", "Malformed PROTOCOL code");
        }

        return packet;
    }

    /**
     * Returns true if could be sent. False if there was a problem
     *
     * @param packet Packet to send over the net
     * @return
     */
    public boolean write_NetworkPacket(Writable packet) {
        try {
            // Packet knows how to write itself. yay
            packet.write(this);
            bos.flush();
            return true;
        } catch (IOException e) {
            System.err.println("CU: Error sending Packet");
            return false;
        }
    }

    private void read_PacketArgs(Packet packet) throws IOException {
        read_bytes(1); // Consume Space

        // TODO Implement reading packet args for every command type
        switch (packet.command) {
            case START:
                packet.putField("id", read_int32());
                break;
            case ANTE:
                packet.putField("ante", read_int32());
                break;
            case STAKES:
                packet.putField("stakes_client", read_int32());
                read_bytes(1);
                packet.putField("stakes_server", read_int32());
                break;
            case DEALER:
                packet.putField("dealer", read_int32());
                break;
            case HAND:
                packet.putField("number", read_int32());
                packet.putField("cards", read_cards(5));
                break;
            case BET:
                packet.putField("chips", read_int32());
                break;
            case RAISE:
                packet.putField("chips", read_int32());
                break;
            case DRAW:
                int drawCount = read_int32();
                System.out.println("COM: read card count " + drawCount);
                packet.putField("number", drawCount);
                if (drawCount > 0)
                    packet.putField("cards", read_cards(drawCount));
                break;
            case DRAW_SERVER:
                // TODO implement method to read DRWS msg
                // PROBLEM : Needs to know if expects cards or not
                break;
            case ERROR:
                packet.putField("error", read_string_variable(2));
                break;

        }
    }

    /* Llegir un enter de 32 bits */
    public int read_int32() throws IOException {
        byte bytes[] = new byte[4];
        bytes = read_bytes(4);

        return bytesToInt32(bytes, "be");
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

        dos.write(bStr, 0, STRSIZE);
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
        dos.write(bHeader, 0, size);
        // Enviem l'string writeBytes de DataOutputStrem no envia el byte més alt dels chars.
        dos.writeBytes(str);
    }

    /**
     * Reads a stream of card codes and returns a Card[] interpretation. Automatically consumes leading
     * spaces.
     * 
     * @param num Number of cards to read
     * @return
     * @throws IOException 
     */
    private String read_cards(int num) throws IOException {
        String[] cards = new String[num];

        for (int i = 0; i < num; i++) {
            read_bytes(1); // Consume space
            char[] cs = new char[3];
            cs[0] = (char)read_bytes(1)[0];
            cs[1] = (char)read_bytes(1)[0];
            
            if (cs[0] == '1') {
                // Read last character for 10X cards
                System.out.println("COM: Detected 10X card.");
                cs[2] = (char)read_bytes(1)[0];
                cards[i] = String.valueOf(cs);
            } else {
                // Cut array to 2 places otherwise
                cards[i] = String.valueOf(Arrays.copyOf(cs, 2));
            }
            
            System.out.println(cs);
            System.out.println("COM: Read card: " + cards[i]);
        }
        return String.join(" ", cards);
    }
}
