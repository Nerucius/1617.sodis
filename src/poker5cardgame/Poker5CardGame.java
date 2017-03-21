package poker5cardgame;

import java.util.Scanner;
import poker5cardgame.game.Card;
import poker5cardgame.game.GameState.Action;
import poker5cardgame.game.Move;
import poker5cardgame.io.NetworkSource;
import poker5cardgame.network.Client;
import poker5cardgame.network.GameServer;
import poker5cardgame.network.Server;

public class Poker5CardGame {

    /**
     * @param args the command line arguments
     */
    public static void main(String... args) {
        // Read console input
        Scanner sc = new Scanner(System.in);
        String line;
        final Client client = new Client();
        Server server = null;

        // Commands
        // quit         : terminate program
        // server       : start server on port 1212
        // connect <ip> : connect to a given IP
        // <ACTION> <args> : send the given action with a list of arguments
        while (true) {
            line = sc.nextLine();
            String[] ls = line.split(" ");

            if (ls[0].equals("quit")) {
                client.close();
                if (server != null) {
                    server.close();
                }
                System.exit(0);
            }

            if (ls[0].equals("server")) {
                // Type "server" to start a server
                server = new GameServer();
                server.bind(1212);
                server.start();

            } else if (ls[0].equals("connect")) {
                // Type "connect" to connecto to localhost
                client.connect(ls[1], 1212);
                if (!client.isConnected()) {
                    continue;
                }

                // Begin our client
                new Thread(new Runnable() {
                    public void run() {
                        ((NetworkSource) client.getSource()).getCom().setTimeout(0);
                        while (client.isConnected()) {
                            System.out.println("Client: Ready to recieve next Move...");
                            Move m = client.getSource().getNextMove();
                            System.out.println(m);
                        }
                    }
                }).start();

            } else if (ls[0].equals("close")) {
                // Type "close" to terminate client
                client.close();

            } else if (client.isConnected()) {

                Card[] cardsEmpty = {};
                Move m = new Move();

                try {
                    m.action = Action.valueOf(ls[0]);
                } catch (IllegalArgumentException e) {
                    System.err.println("Not a Move: " + ls[0]);
                    continue;
                }

                m.cards = cardsEmpty;
                if (ls.length > 1) {
                    m.id = Integer.valueOf(ls[1]);
                    m.chips = Integer.valueOf(ls[1]);
                    m.dealer = 1;
                    m.cStakes = Integer.valueOf(ls[1]);
                    m.error = ls[1];
                    m.cDrawn = 0;
                }
                if (ls.length > 2) {
                    try {
                        // CASE DRAW # CARDS
                        String cardsStr = "";
                        for (int i = 2; i < ls.length; i++) {
                            cardsStr += String.valueOf(ls[i]) + " ";
                        }
                        m.cards = NetworkSource.cardsFromCodeString(cardsStr);
                        m.cDrawn = Integer.valueOf(ls[1]);

                    } catch (Exception e) {}
                }
                client.getSource().sendMove(m);
            }
        }
    }
}
