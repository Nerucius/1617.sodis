package poker5cardgame;

import java.util.Scanner;
import poker5cardgame.game.Game;
import poker5cardgame.network.Client;
import poker5cardgame.network.Server;

public class Poker5CardGame {

    /**
     * @param args the command line arguments
     */
    public static void main(String... args) {
        // Read console input
        Scanner sc = new Scanner(System.in);
        String line;
        Client c = null;

        while (!(line = sc.nextLine()).equals("q")) {
            String[] ls = line.split(" ");

            if (ls[0].equals("server")) {
                Server server = new Server();
                server.bind(1212);
                server.start();

            } else if (ls[0].equals("connect")) {
                // Type "connect" to connecto to localhost
                c = new Client();
                c.connect(ls[1], 1212);
                
                
            } else if (ls[0].equals("close")) {
                // Type "close" to terminate client
                c.close();
                c = null;

            } else if (c != null) {

                Game.Move m = new Game.Move();
                m.action = Game.Action.valueOf(ls[0]);
                if (ls.length > 1)
                    m.id = Integer.valueOf(ls[1]);
                if (ls.length > 1)
                    m.chips = Integer.valueOf(ls[1]);
                if (ls.length > 1)
                    m.dealer = Integer.valueOf(ls[1]);
                if (ls.length > 1)
                    m.cStakes = Integer.valueOf(ls[1]);
                if (ls.length > 2)
                    m.sStakes = Integer.valueOf(ls[2]);

                c.getOutSource().sendMove(m);

            }
        }

    }
}
