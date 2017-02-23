/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.game;


/**
 * Finite State Machine for the Game State
 */
public class Game {
    
    protected GameState state;
    
    // TODO Evaluate best possible state list
    public enum GameState{
        START,
        ANTE,
        DEAL,
        BET,
        DRAW,
        SHOW,
        END,
    }
    
    public Game(){
        this.state = GameState.START;
    }
    
    public GameState getState(){
        return state;
    }
    
}
