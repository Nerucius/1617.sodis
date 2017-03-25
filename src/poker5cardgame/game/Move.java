/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.game;

import java.util.Arrays;
import poker5cardgame.game.GameState.Action;


/**
* Class defining a move made by either player, affecting the game.
*/
public class Move {

    public Action action;
    // Client Game ID
    public int id = -1;
    // Generic chips param for ANTE, BET or RAISE
    public int chips = -1;
    // Client and server Stakes
    public int cStakes = -1, sStakes = -1;
    // Dealer Flag, 1 for Client dealer, 0 for server dealer
    public int dealer = -1;
    // Winner Flag, 1 for Client winner, 0 for server winner
    public int winner = -1;
    // Array of cards to deal or discard
    public Card[] cards = null;
    // Number of cards discarded
    public int cDrawn = -1, sDrawn = -1;

    // Error Message
    public String error = null;

    public Move() {
        action = Action.NOOP;
    }

    @Override
    public String toString() {
        String str = action.toString()
                + (id == -1 ? "" : " id:" + id)
                + (chips == -1 ? "" : " chips:" + chips)
                + (cStakes == -1 ? "" : " cStakes:" + cStakes)
                + (sStakes == -1 ? "" : " sStakes:" + sStakes)                
                + (cDrawn == -1 ? "" : " cDrawn:" + cDrawn)
                + (sDrawn == -1 ? "" : " sDrawn:" + sDrawn)
                + (dealer == -1 ? "" : " dealer:" + dealer)
                + (winner == -1 ? "" : " winner:" + winner)
                + (cards == null ? "" : " cards:" + Arrays.toString(cards))
                + (error == null ? "" : " err:" + error);
        return str;
    }
}