package poker5cardgame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import poker5cardgame.game.Card;
import poker5cardgame.game.Card.Rank;
import poker5cardgame.game.Card.Suit;
import poker5cardgame.game.Deck;
import poker5cardgame.game.KeyboardSource;
import poker5cardgame.game.Game;
import poker5cardgame.game.Hand;
import poker5cardgame.game.HandRanker;
import poker5cardgame.game.HandRanker.HandRank;
import poker5cardgame.network.Client;
import poker5cardgame.network.Server;

public class Poker5CardGame {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {

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
        
        Hand sHand = new Hand();
        Hand cHand = new Hand();
        
        sHand.putCard(new Card(Suit.CLUBS, Rank.ACE));
        sHand.putCard(new Card(Suit.CLUBS, Rank.TWO));
        sHand.putCard(new Card(Suit.CLUBS, Rank.THREE));
        sHand.putCard(new Card(Suit.CLUBS, Rank.FOUR));
        sHand.putCard(new Card(Suit.HEARTS, Rank.SIX));
        //sHand.computeHandValue();
        sHand.generateRankerInformation();
        
        cHand.putCard(new Card(Suit.CLUBS, Rank.ACE));
        cHand.putCard(new Card(Suit.CLUBS, Rank.TWO));
        cHand.putCard(new Card(Suit.CLUBS, Rank.THREE));
        cHand.putCard(new Card(Suit.CLUBS, Rank.FOUR));
        cHand.putCard(new Card(Suit.HEARTS, Rank.FIVE));
        /*cHand.putCard(new Card(Suit.DIAMONDS, Rank.ACE));
        cHand.putCard(new Card(Suit.DIAMONDS, Rank.KING));
        cHand.putCard(new Card(Suit.DIAMONDS, Rank.QUEEN));
        cHand.putCard(new Card(Suit.DIAMONDS, Rank.JACK));
        cHand.putCard(new Card(Suit.HEARTS, Rank.NINE));*/
        //cHand.computeHandValue();
        cHand.generateRankerInformation();

        System.out.println("sHand vs cHand ::: " + sHand.compareTo(cHand));
        
        /*Map dict = new HashMap();
        dict.put(Rank.TWO, 1);
        dict.put(Rank.THREE, 1);
        dict.put(Rank.FOUR, 1);
        dict.put(Rank.ACE, 1);        
        System.out.println(dict.toString());
        
        Map dict2 = new HashMap();
        dict2.put(Rank.TWO, 1);
        dict2.put(Rank.JACK, 1);
        dict2.put(Rank.FOUR, 1);
        dict2.put(Rank.FIVE, 1);
        System.out.println(dict2.toString());
        
        Map d = new HashMap(dict);
        d.putAll(dict2);
        for(Object key : d.keySet())
        {
            if(dict2.containsKey(key) && dict.containsKey(key))
                d.put(key, (int)dict2.get(key) + (int)dict.get(key));
        }
        
        System.out.println(d.toString());*/
        
        
        // Server
        
        /*Server server = new Server();
        server.bind(1212);
        server.start();

        
        Client client = new Client();
        client.connect("127.0.0.1", 1212);
        client.writeMsg("Hello World");
        client.writeMsg("maths r cool");*/
         
    }

}
