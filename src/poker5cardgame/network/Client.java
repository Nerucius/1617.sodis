package poker5cardgame.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    ComUtils com = null;

    public Client() {

    }

    public static void main(String... args) {
        Scanner sc = new Scanner(System.in);
        String line;
        Client c = null;

        while (!(line = sc.nextLine()).equals("q")) {
            if (line.equals("connect")) {
                c = new Client();
                c.connect("localhost", 1212);
            } else if (c != null) {
                c.writeMsg(line);
            }
        }

    }

    public void connect(String IP, int port) {
        try {
            InetAddress address = InetAddress.getByName(IP);
            Socket sock = new Socket(address, port);
            com = new ComUtils(sock);

        } catch (Exception ex) {
            System.err.println("Client: Failed to connect to Server on IP:" + IP + ".");
        }

    }

    public void close() {
        if (com == null) {
            System.err.println("Client: Not connected. Can't close()");
            return;
        }

        try {
            Socket sock = com.getSocket();
            sock.close();
            com = null;
        } catch (IOException ex) {
            System.err.println("Client: Error while closing Socket.");
        }
    }

    public void writeMsg(String str) {
        try {
            com.write_string_variable(4, str);
        } catch (IOException ex) {
            System.err.println("Connection closed.");
        }
    }

}
