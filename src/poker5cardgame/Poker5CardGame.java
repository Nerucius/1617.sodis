package poker5cardgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import poker5cardgame.game.Card;
import poker5cardgame.game.Deck;
import poker5cardgame.game.Game;
import poker5cardgame.game.Hand;
import poker5cardgame.game.HandRanker;
import poker5cardgame.game.HandRanker.HandRank;

public class Poker5CardGame {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try {
            Deck deck = new Deck();
            Hand hand = new Hand();
            hand.draw5FromDeck(deck);
            System.out.println("Hand = " + hand);
            System.out.println("HandRank = " + HandRanker.getHandRank(hand));

            
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        
        /*HandRank handA = HandRank.ONE_PAIR;
        HandRank handB = HandRank.TWO_PAIR;

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
        game.apply(Game.Action.QUIT);*/

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
