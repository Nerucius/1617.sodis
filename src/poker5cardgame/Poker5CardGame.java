package poker5cardgame;

import poker5cardgame.game.Card;
import poker5cardgame.game.Card.Rank;
import poker5cardgame.game.Card.Suit;
import poker5cardgame.game.KeyboardSource;
import poker5cardgame.game.Game;
import poker5cardgame.game.Hand;
import poker5cardgame.game.HandRanker;
import poker5cardgame.game.HandRanker.HandRank;

public class Poker5CardGame {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        
        
        Hand handA = new Hand();
        handA.putCard(new Card(Card.Suit.DIAMONDS, Card.Rank.TWO));
        handA.putCard(new Card(Card.Suit.DIAMONDS, Card.Rank.THREE));
        handA.putCard(new Card(Card.Suit.DIAMONDS, Card.Rank.FOUR));
        handA.putCard(new Card(Card.Suit.DIAMONDS, Card.Rank.FIVE));
        handA.putCard(new Card(Card.Suit.DIAMONDS, Card.Rank.SIX));
        
        HandRank r = HandRanker.getHandRank(handA);
        
        System.out.println(r);
        
        /*Game game = new Game(new KeyboardSource());
        
        while(game.getState() != Game.State.QUIT){
            game.update();
            Thread.sleep(200);
        }*/
        
        
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
