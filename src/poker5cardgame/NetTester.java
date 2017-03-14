/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame;

import poker5cardgame.game.Card;
import poker5cardgame.game.GameState.Action;
import poker5cardgame.game.Move;
import poker5cardgame.network.Client;
import poker5cardgame.network.EchoServer;
import poker5cardgame.network.Server;

/**
 *
 * @author Akira
 */
public class NetTester {

    static public void main(String... args) throws Exception {

        Server s = new EchoServer();
        s.bind(1212);
        s.start();

        Client c = new Client();
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
        //Move reply = c.getSource().getNextMove();
        //System.out.println(reply);

        Thread.sleep(100000);

        c.close();
        s.close();

    }

}
