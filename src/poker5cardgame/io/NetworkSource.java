/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.io;

import java.io.IOException;
import java.net.Socket;
import poker5cardgame.game.Card;
import poker5cardgame.game.GameState.Action;
import poker5cardgame.game.Move;
import poker5cardgame.network.Network;
import poker5cardgame.network.Packet;

/**
 * Class Implementing the Source Interface for sending and receiving Moves using
 * a Network Connection.
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


    public Move getNextMove() {
        System.out.println("[DEBUG NetworkSource]: getNextMove");
        
        Move move = new Move();

        if (comUtils == null) {
            // Connection has been closed. Notify the Game
            move.action = Action.TERMINATE;
            return move;
        }

        // Read the next full packet from the network. This method
        // never fails or throws exception. If the network connection
        // breaks the packet will just say NET_ERROR.
        Packet packet = comUtils.read_NetworkPacket();

        try {
            switch (packet.command) {
                case START:
                    move.action = Action.START;
                    move.id = packet.getField("id", Integer.class);
                    break;
                case ANTE:
                    move.chips = packet.getField("chips", Integer.class);
                    
                    // Request the next Move directly, which should be a STAKES
                    Move stks = getNextMove();
                    if(stks.action != Action.STAKES){
                        System.err.println("Invalid packet after ANTE");
                        move.action = Action.NOOP;
                        return move;
                    }
                    
                    move.action = Action.ANTE_STAKES;
                    move.cStakes = stks.cStakes;
                    move.sStakes = stks.sStakes;
                    break;
                case STAKES:
                    move.action = Action.STAKES;
                    move.cStakes = packet.getField("stakes_client", Integer.class);
                    move.sStakes = packet.getField("stakes_server", Integer.class);
                    break;
                case ANTE_OK:
                    move.action = Action.ANTE_OK;
                    break;
                case QUIT:
                    move.action = Action.QUIT;
                    break;
                case DEALER:
                    move.action = Action.DEALER_HAND;
                    move.dealer = packet.getField("dealer", Integer.class);
                    Move hand = getNextMove();
                    if(hand.cards == null){
                        System.err.println("Invalid packet after DEALER");
                        move.action = Action.NOOP;
                        return move;
                    }
                    move.cards = hand.cards;
                    break;                    
                case HAND:
                    move.action = Action.DEALER_HAND;
                    move.cards = cardsFromCodeString(packet.getField("cards", String.class));
                    break;
                    
                case RAISE:
                    move.action = Action.RAISE;
                    move.chips = packet.getField("chips", Integer.class);
                    break;
                case BET:
                    move.action = Action.BET;
                    move.chips = packet.getField("chips", Integer.class);
                    break;
                    
                case DRAW:
                    // If any cards were requested, get the Card array
                    move.action = Action.DRAW;
                    move.cDrawn = packet.getField("number", Integer.class);
                    if (move.cDrawn > 0)
                        move.cards = cardsFromCodeString(packet.getField("cards", String.class));
                    break;
                case DRAW_SERVER:
                    move.action = Action.DRAW_SERVER;
                    move.cards = cardsFromCodeString(packet.getField("cards", String.class));
                    // TODO @alex/client Save this value and inform the client of how many cards the server requested
                    move.sDrawn = packet.getField("number", Integer.class);
                    break;
                    
                case SHOWDOWN:
                    move.action = Action.SHOW;
                    move.cards = cardsFromCodeString(packet.getField("cards", String.class));
                    break;
                    
                case ERROR:
                    move.action = Action.ERROR;
                    move.error = packet.getField("error", String.class);
                    break;
                    
                case NET_ERROR:
                    // Irrecoverable network error. Terminate the Client Game
                    move.action = Action.TERMINATE;
                    break;
                    
                case PASS:
                    move.action = Action.PASS;
                    break;
                case CALL:
                    move.action = Action.CALL;
                    break;
                case FOLD:
                    move.action = Action.FOLD;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Network Source: Problem reading next Move arguments");
            move.action = Action.NOOP;
        }

        return move;
    }

    /**
     * Transforms a game Action into a Network Packet and sends it over. If
     * this method returns FALSE, the Source has been terminated. You can handle
     * the termination right there. Or on the next call to getNextMove() you'll
     * get an Action.TERMINATE move.
     *
     * @param move Move to send over the Network.
     */
    public boolean sendMove(Move move) {
        System.out.println("[DEBUG NetworkSource]: sendMove");

                
        // Define an array as large as the most packets sent by a single Move
        // Some moves send more than one packet.
        Packet[] packets = new Packet[2];

        switch (move.action) {
            case START:
                packets[0] = new Packet(Network.Command.START);
                packets[0].putField("id", move.id);
                break;

            case ANTE_STAKES:
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
                packets[0].putField("dealer", move.dealer);

                packets[1] = new Packet(Network.Command.HAND);
                packets[1].putField("cards", cardsToCodeString(move.cards));
                break;

            case PASS:
                packets[0] = new Packet(Network.Command.PASS);
                break;

            case BET:
                packets[0] = new Packet(Network.Command.BET);
                packets[0].putField("chips", move.chips);
                break;

            case RAISE:
                packets[0] = new Packet(Network.Command.RAISE);
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
                // Create a list of arguments as follows: DRWS 2C 3H 4D '2'
                packets[0].putField("cards", cardsToCodeString(move.cards));
                packets[0].putField("number", move.sDrawn);
                break;

            case SHOW:
                packets[0] = new Packet(Network.Command.SHOWDOWN);
                packets[0].putField("cards", cardsToCodeString(move.cards));
                break;
                
            case ERROR:
                packets[0] = new Packet(Network.Command.ERROR);
                packets[0].putWrittable(new Writable.VariableString(2, move.error));                
        }

        // Write all packets to the network
        for (Packet packet : packets) {
            if (packet != null)
                // TODO Deal with exception inside try-catch
                
                
                if (!comUtils.write_NetworkPacket(packet)) {
                    // Something has gone wrong. Terminate everything
                    try {
                        comUtils.getSocket().close();
                    } catch (IOException ex) {
                        System.err.println("Network: Error while closing connection.");
                    }
                    comUtils = null;
                    return false;
                }
        }
        return true;
    }

    public ComUtils getCom() {
        return comUtils;
    }

    /**
     * Convert an array of cards to a String of card codes separated by " "
     */
    public static String cardsToCodeString(Card[] cards) {        
        String[] codes = new String[cards.length];

        for (int i = 0; i < cards.length; i++)
            codes[i] = cards[i].getCode();

        return String.join(" ", codes);
    }

    /**
     * Convert an array of cards to an array of card codes
     */
    public static Card[] cardsFromCodeString(String cards) {
        String[] cardsplits = cards.split(" ");
        
        // Returns zero lenght arrays some times -> its OK
        Card[] arr = new Card[cardsplits.length];
        for (int i = 0; i < cardsplits.length; i++) {
            arr[i] = Card.fromCode(cardsplits[i]);
        }
        return arr;
    }

}
