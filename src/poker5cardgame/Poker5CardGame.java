package poker5cardgame;

import poker5cardgame.game.Game;
import poker5cardgame.game.Ranker.Rank;

public class Poker5CardGame {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Rank handA = Rank.PAIR;
        Rank handB = Rank.DOUBLE_PAIR;

        if (handA.wins(handB)) {
            System.out.println(handA + " Wins " + handB);
        } else if (handA.loses(handB)) {
            System.out.println(handB + " Wins " + handA);
        }
        
        
        Game game = new Game();
               
        game.apply(Game.Action.START);
        game.apply(Game.Action.ANTE_STAKES);
        game.apply(Game.Action.ANTE_OK);
        game.apply(Game.Action.DEALER_HAND);
        game.apply(Game.Action.BET);
        game.apply(Game.Action.RAISE);
        game.apply(Game.Action.RAISE);
        game.apply(Game.Action.START);
        game.apply(Game.Action.CALL);
        game.apply(Game.Action.DRAW);
        game.apply(Game.Action.DRAW_SERVER);
        game.apply(Game.Action.PASS);
        game.apply(Game.Action.PASS);
        game.apply(Game.Action.STAKES);
        game.apply(Game.Action.QUIT);

        // Server
        /*
        Server server = new Server();
        server.bind(1212);
        server.start();

        
        Client client = new Client();
        client.connect("127.0.0.1", 1212);
        Client client2 = new Client();
        client2.connect("127.0.0.1", 1212);    
         */
    }

}
