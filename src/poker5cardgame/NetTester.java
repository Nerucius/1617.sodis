// TODO delete class
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame;

import poker5cardgame.game.Card;
import poker5cardgame.game.GameState.Action;
import poker5cardgame.game.Move;
import poker5cardgame.game.ClientGame;
import poker5cardgame.io.ComUtils;
import poker5cardgame.io.KeyboardSource;
import poker5cardgame.io.NetworkSource;
import poker5cardgame.network.MTGameServer;
import poker5cardgame.network.MultithreadServer;
import poker5cardgame.network.SGameServer;
import poker5cardgame.network.Server;

/**
 *
 * @author Akira
 */
public class NetTester {

    static public void main(String... args) throws Exception {

        Server s = new SGameServer();
        s.bind(1212);
        s.start();
        
        ClientGame c = new ClientGame(new KeyboardSource());
        c.connect("localhost", 1212);

        while(c.isConnected()){
            c.update();
        }
        
        /*
        GameClient c = new GameClient();
        c.connect("localhost", 1212);

        // Test Array
        Card[] cards = new Card[5];
        cards[0] = Card.fromCode("10S");
        cards[1] = Card.fromCode("2S");
        cards[2] = Card.fromCode("3S");
        cards[3] = Card.fromCode("4S");
        cards[4] = Card.fromCode("5S");

        Move move = new Move();
        move.action = Action.SHOW;
        move.cards = cards;

        c.getSource().sendMove(move);
        while (c.isConnected()) {
            Move reply = c.getSource().getNextMove();
            if (reply.action == Action.TERMINATE)
                break;
            System.out.println("Client got: " + reply);
        }
        */

        c.close();
        s.close();

    }

}
