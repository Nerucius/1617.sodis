/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.io;

import poker5cardgame.io.Source;
import java.io.IOException;
import java.net.Socket;
import poker5cardgame.game.Card;
import poker5cardgame.game.Game;
import poker5cardgame.game.Game.Action;
import poker5cardgame.game.Game.Move;
import poker5cardgame.network.Network;
import poker5cardgame.network.Packet;

/**
 *
 * @author Herman Dempere
 */
public class NetworkSource implements Source {

    private ComUtils comUtils;

    public NetworkSource(Socket socket) {
        try {
            comUtils = new ComUtils(socket);
        } catch (Exception ex) {
            System.err.println("NETWORK SORUCE: Could not Open Socket");
            ex.printStackTrace();
        }
    }

    /**
     * Transforms a Network Packet into a move
     *
     * @return Next move provided by network.
     */
    public Move getNextMove() {
        Move move = new Game.Move();

        if (comUtils == null) {
            // Connection has been closed. Notify the Game
            move.action = Action.TERMINATE;
            return move;
        }

        Packet packet = comUtils.read_NetworkPacket();

        // TODO Fill in all cases
        switch (packet.command) {
            case START:
                move.action = Action.CLIENT_START;
                //move.id = Integer.valueOf(packet.args[0]);
                break;

            case ANTE:
                break;
            case STAKES:
                break;

            case ANTE_OK:
                move.action = Action.ANTE_OK;
                break;
            case QUIT:
                move.action = Action.QUIT;
                break;
            case DEALER:
                move.action = Action.DEALER_HAND;
                //move.dealer = Integer.valueOf(packet.arg/s[0]);
                Move next = getNextMove();
                break;
            case HAND:
                move.action = Action.DEALER_HAND;
                //move.cards = cardsFromCodeString(packet.args[0]);
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
                //System.err.println("PROTOCOL ERROR: " + packet.args[0]);
                move.action = Action.NOOP;
                break;
            case NET_ERROR:
                // TODO Handle connection closed
                move.action = Action.TERMINATE;
                break;
        }

        return move;
    }

    /**
     * Transforms a game Action into a Network Packet and sends it over.
     *
     * @param move
     */
    public void sendMove(Game.Move move) {
        // Define an array as large as the most packets sent by a single Move
        // Some moves send more than one packet.
        Packet[] packets = new Packet[2];

        switch (move.action) {
            case CLIENT_START:
                packets[0] = new Packet(Network.Command.START);
                packets[0].putField("id", move.id);
                break;

            case SEND_ANTE_STAKES:
                packets[0] = new Packet(Network.Command.ANTE);
                packets[0].putField("chips", move.chips);

                packets[1] = new Packet(Network.Command.STAKES);
                packets[1].putField("stakes_client", move.cStakes);
                packets[1].putField("stakes_server", move.sStakes);
                break;

            case STAKES:
                packets[0] = new Packet(Network.Command.STAKES);
                packets[0].putField("stakes_client", move.cStakes);
                packets[0].putField("stakes_server", move.sStakes);
                break;

            case QUIT:
                packets[0] = new Packet(Network.Command.QUIT);
                break;

            case ANTE_OK:
                packets[0] = new Packet(Network.Command.ANTE_OK);
                break;

            case DEALER_HAND:
                packets[0] = new Packet(Network.Command.DEALER);
                packets[0].putField("chips", move.dealer);

                packets[1] = new Packet(Network.Command.HAND);
                packets[1].putField("hand", cardsToCodeString(move.cards));
                break;

            case PASS:
                packets[0] = new Packet(Network.Command.PASS);
                break;

            case BET:
                packets[0] = new Packet(Network.Command.BET);
                packets[0].putField("chips", move.chips);
                break;

            case RAISE:
                packets[0] = new Packet(Network.Command.BET);
                packets[0].putField("chips", move.chips);
                break;

            case FOLD:
                packets[0] = new Packet(Network.Command.FOLD);
                break;

            case CALL:
                packets[0] = new Packet(Network.Command.CALL);
                break;

            case DRAW:
                packets[0] = new Packet(Network.Command.DRAW);
                // Create a list of arguments as follows: '2' 2C 3H
                packets[0].putField("number", move.cards.length);
                packets[0].putField("cards", cardsToCodeString(move.cards));
                break;

            case DRAW_SERVER:
                packets[0] = new Packet(Network.Command.DRAW_SERVER);
                // Create a list of arguments as follows: 2C 3H 4D '2'
                packets[0].putField("cards", cardsToCodeString(move.cards));
                packets[0].putField("number", move.dealer);
                break;

            case SHOW:
                packets[0] = new Packet(Network.Command.SHOWDOWN);
                break;
        }

        // Write all packets to the network
        for (Packet packet : packets) {
            if (packet != null)

                if (!comUtils.write_NetworkPacket(packet)) {
                    // Something has gone wrong. Terminate everything
                    try {
                        comUtils.getSocket().close();
                    } catch (IOException ex) {
                        System.err.println("Network: Error while closing connection.");
                    }
                    comUtils = null;
                }
        }
    }

    public ComUtils getCom() {
        return comUtils;
    }

    /**
     * Convert an array of cards to a String of card codes separated by " "
     */
    private String cardsToCodeString(Card[] cards) {
        String[] codes = new String[cards.length];

        for (int i = 0; i < cards.length; i++)
            codes[i] = cards[i].getCode();

        return String.join(" ", codes);
    }

    /**
     * Convert an array of cards to an array of card codes
     */
    private Card[] cardsFromCodeString(String cards) {
        String[] cardsplits = cards.split(" ");
        Card[] arr = new Card[cardsplits.length];
        for (int i = 0; i < cardsplits.length; i++) {
            arr[i] = Card.fromCode(cardsplits[i]);
        }
        return arr;
    }

}
