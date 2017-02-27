package poker5cardgame.game;

import java.util.HashMap;
import java.util.Map;


/**
 * Finite State Machine for the Game State
 */
public class Game {
    
    protected State state = null;
    
    public enum Action{
        START,
        ANTE_STAKES,
        STAKES,
        QUIT,
        ANTE_OK,
        DEALER_HAND,
        PASS,
        BET,
        RAISE,
        FOLD,
        CALL,
        DRAW,
        DRAW_SERVER
    }
    
    // TODO Evaluate best possible state list
    public enum State{
        START(Action.ANTE_STAKES),
        ANTE(Action.ANTE_OK, Action.QUIT),
        PLAY(Action.DEALER_HAND),
        BETTING(Action.BET, Action.PASS),
        BETTING_DEALER(Action.BET, Action.PASS),
        COUNTER(Action.FOLD, Action.RAISE, Action.CALL),
        DRAW(Action.DRAW),
        DRAW_SERVER(Action.DRAW_SERVER),
        SHOWDOWN(Action.STAKES);
                
        Action[] allowedActions;
        Map<Game.Action,Game.State> transitions = new HashMap<>();
        
        private State(Action... allowed){
            allowedActions = allowed;
            
        }
    }
    
    static{
        
        Map<Game.Action,Game.State> transitions = new HashMap<>();
        transitions.put(Action.PASS, State.BETTING);
        transitions.put(Action.PASS, State.BETTING);
        
    }
    
    public Game(){
    }
    
    public void apply(Game.Action action){
        
    }
    
    public Game.State getState(){
        return state;
    }
    
}
