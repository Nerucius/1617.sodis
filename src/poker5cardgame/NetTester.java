/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame;

import poker5cardgame.game.Game;
import poker5cardgame.game.Game.Move;
import poker5cardgame.network.Client;
import poker5cardgame.network.EchoServer;
import poker5cardgame.network.Server;

/**
 *
 * @author Akira
 */
public class NetTester {

    static public void main(String... args) throws Exception{

        Server s = new EchoServer();
        s.bind(1212);
        s.start();

        Client c = new Client();
        c.connect("localhost", 1212);
        
        Move move = new Game.Move();
        move.action = Game.Action.BET;
        move.chips = 50;
        
        c.getSource().sendMove(move);
        //Move reply = c.getSource().getNextMove();
        //System.out.println(reply);
        
        Thread.sleep(100000);
        
        c.close();
        s.close();

    }

}
