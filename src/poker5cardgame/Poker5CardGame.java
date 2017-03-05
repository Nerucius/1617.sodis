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
        // Start a server
        Server server = new Server();
        server.bind(1212);
        server.start();

        // Prepare parameters for a client
        Scanner sc = new Scanner(System.in);
        String line;
        Client c = null;

        while (!(line = sc.nextLine()).equals("q")) {

            // Type "connect" to connecto to localhost
            if (line.equals("connect")) {
                c = new Client();
                c.connect("localhost", 1212);
            } else if (c != null) {

                String[] ls = line.split(" ");

                Game.Move m = new Game.Move();
                m.action = Game.Action.valueOf(ls[0]);
                if(ls.length > 1) m.id = Integer.valueOf(ls[1]);
                if(ls.length > 1) m.chips = Integer.valueOf(ls[1]);
                if(ls.length > 1) m.dealer = Integer.valueOf(ls[1]);
                if(ls.length > 1) m.cStakes = Integer.valueOf(ls[1]);
                if(ls.length > 2) m.sStakes = Integer.valueOf(ls[2]);
                
                c.getOutSource().sendMove(m);

            }
        }

    }
}
