/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.io;

import poker5cardgame.game.Game.Move;

/**
 *
 * @author Akira
 */
public interface Source {
    
    /** Method to get the next move **/
    public Move getNextMove();
    
    /** Method to inform of a move to the opponent **/
    public void sendMove(Move move);
    
}
