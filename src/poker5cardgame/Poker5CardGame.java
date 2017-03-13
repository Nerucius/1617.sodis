package poker5cardgame;

import java.util.Scanner;
import poker5cardgame.game.Card;
import poker5cardgame.game.Game;
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
                if (server != null)
                    server.close();
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
                if (!client.isConnected())
                    continue;

                // Begin our client
                new Thread(new Runnable() {
                    public void run() {
                        ((NetworkSource) client.getSource()).getCom().setTimeout(0);
                        while (client.isConnected()) {
                            System.out.println("Client: Ready to recieve next Move...");
                            Game.Move m = client.getSource().getNextMove();
                            System.out.println(m);
                        }
                    }
                }).start();

            } else if (ls[0].equals("close")) {
                // Type "close" to terminate client
                client.close();

            } else if (client.isConnected()) {

                // test card array
                Card[] cards = new Card[5];
                cards[0] = Card.fromCode("10S");
                cards[1] = Card.fromCode("2S");
                cards[2] = Card.fromCode("3S");
                cards[3] = Card.fromCode("4S");
                cards[4] = Card.fromCode("5S");

                Game.Move m = new Game.Move();

                try {
                    m.action = Game.Action.valueOf(ls[0]);
                } catch (IllegalArgumentException e) {
                    System.err.println("Not a Move: " + ls[0]);
                    continue;
                }
                
                m.cards = cards;
                if (ls.length > 1) {
                    m.id = Integer.valueOf(ls[1]);
                    m.chips = Integer.valueOf(ls[1]);
                    m.dealer = 1;
                    m.cStakes = Integer.valueOf(ls[1]);
                    m.error = ls[1];
                }
                if (ls.length > 2) {
                    
                    try {
                        // Case: STKS CHIPS CHIPS
                        m.sStakes = Integer.valueOf(ls[2]);
                    } 
                    catch(NumberFormatException nfe)
                    {
                        String cardsStr = "";
                        try
                        {   // CASE DRAW # CARDS
                            for(int i = 2; i < ls.length; i++)
                                cardsStr += String.valueOf(ls[i]) + " ";

                            m.cards = NetworkSource.cardsFromCodeString(cardsStr);
                        }
                        catch(Exception e)
                        {
                            // Case DRWS CARDS #
                            for(int i = 1; i < ls.length-2; i++)
                                cardsStr += String.valueOf(ls[i]) + " ";
                            cardsStr += String.valueOf(ls[ls.length-1]);
                            m.cards = NetworkSource.cardsFromCodeString(cardsStr);
                        }                       
                    }
                }

                client.getSource().sendMove(m);

            }
        }

    }
}
