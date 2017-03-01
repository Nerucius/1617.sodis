/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.game;

import java.io.IOException;
import java.net.Socket;
import poker5cardgame.game.Game.Action;
import poker5cardgame.network.ComUtils;
import poker5cardgame.network.Network;
import poker5cardgame.network.Network.Packet;

/**
 *
 * @author Akira
 */
public class NetworkSource implements Source {

    private ComUtils network;

    public NetworkSource(Socket socket) {
        try {
            network = new ComUtils(socket);
        } catch (IOException ex) {
            System.err.println("NETWORK SORUCE: Could not Open Socket");
            ex.printStackTrace();
        }
    }

    /** Transforms a Network Packet into a move
     * @return Next move provided by network.
     */
    public Game.Move getNextMove() {
        Game.Move move = new Game.Move();

        Packet pkt = network.read_NetworkPacket();

        // TODO Fill in all cases
        switch (pkt.command) {
            case START:
                break;
            case ANTE:
                break;
            case STAKES:
                break;
            case ANTE_OK:
                break;
            case QUIT:
                break;
            case DEALER:
                break;
            case HAND:
                break;
            case PASS:
                break;
            case BET:
                break;
            case CALL:
                break;
            case FOLD:
                break;
            case DRAW:
                break;
            case DRAW_SERVER:
                break;
            case SHOWDOWN:
                break;
            case ERROR:
                System.err.println("NETWORK ERROR: " + pkt.args[0]);
                move.action = Action.NOOP;
                break;
        }

        return move;
    }

    /** 
     * Transforms a game Action into a Network Packet and sends it over.
     * @param move 
     */
    public void sendMove(Game.Move move) {
        // TODO create and send a network Packet constructed with the move
        Network.Packet packet = null;

        switch (move.action) {
            case START:
                break;
            case ANTE_STAKES:
                break;
            case STAKES:
                break;
            case QUIT:
                break;
            case ANTE_OK:
                break;
            case DEALER_HAND:
                break;
            case PASS:
                break;
            case BET:
                break;
            case RAISE:
                break;
            case FOLD:
                break;
            case CALL:
                break;
            case DRAW:
                break;
            case DRAW_SERVER:
                break;
            case SHOW:
                break;
        }
    }
    
    public ComUtils getNetwork(){
        return network;
    }

}
