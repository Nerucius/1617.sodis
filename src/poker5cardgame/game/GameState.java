package poker5cardgame.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static poker5cardgame.Log.*;

/**
 * Finite State Machine for the Game State
 */
public class GameState {
    
    public State state = State.INIT;
    private boolean fold = false;
    private boolean secondRound = false;
    private boolean serverTurn; // to control the dealer round
    private boolean showTime = false;

    public boolean isFold() {
        return fold;
    }

    public void setFold(boolean fold) {
        this.fold = fold;
    }

    public boolean isSecondRound() {
        return secondRound;
    }

    public void setSecondRound(boolean secondRound) {
        this.secondRound = secondRound;
    }

    public boolean isServerTurn() {
        return serverTurn;
    }

    public void setServerTurn(boolean serverTurn) {
        this.serverTurn = serverTurn;
    }

    public boolean isShowTime() {
        return showTime;
    }

    public void setShowTime(boolean showTime) {
        this.showTime = showTime;
    }

    @Override
    public String toString() {
        return "GameState{" + "state=" + state + ", serverTurn=" + serverTurn + '}';
    }
    
    // Define all the game actions
    public enum Action {
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
        DRAW_SERVER,
        SHOW,
        // Special no-operation command to do nothing
        NOOP,
        // Special command to send an error message to the client
        ERROR,
        // Special command to terminate the game anytime
        TERMINATE;
    }

    // Define all the game states
    public enum State {
        INIT,
        START,
        ACCEPT_ANTE,
        PLAY,
        BETTING,
        BETTING_DEALER,
        COUNTER,
        DRAW,
        DRAW_SERVER,
        SHOWDOWN,
        QUIT;

        // Define a map with the valid state -> action -> state combinations
        // This map contains a Game.State as value (the next state after applying 
        // a specific action) and a Game.Action as key (the specific applied action)
        public Map<Action, State> transitions;

        private State() {
            transitions = new HashMap<>();
        }
    }
    
    /**
     * Set to the game state the next game state after applying a specific
     * action to the actual game state.
     *
     * @param action Action that is applied to the actual game state
     */
    public void apply(Action action) {
        // If the action is noop, do nothing
        if (action == Action.NOOP)
            return;
        
        // If the action is terminate, finish the game
        if (action == Action.TERMINATE) {
            System.err.println("TERMINATE You exit the game. Thanks for playing!");
            state = State.QUIT;
            return;
        }

        // If the action we want to apply to the actual state is valid, set the next state as actual
        if (state.transitions.containsKey(action)) {
            if (action == Action.STAKES)
                secondRound = false; // set again to false after the second round 
            
            // We finish a round in the next cases: 
            // 1) applying a PASS action being in a BETTING_DEALER state
            // 2) applying a CALL action being in a COUNTER state
            if ((state == State.BETTING_DEALER && action == Action.PASS) ||
                (state == State.COUNTER && action == Action.CALL)) 
            {
                if (!secondRound) // Case: next to DRAW               
                {
                    secondRound = true;
                } else // Case: next to SHOWDOWN
                {
                    // Apply the action show after finishing the 2nd round
                    showTime = true;
                    //state = State.SHOWDOWN;
                    this.apply(Action.SHOW);
                    return;
                }
            }

            // Set the next state as actual
            State nextState = state.transitions.get(action);
            state = nextState;
        } 
        else if(action != Action.ERROR && action != Action.SHOW)
        {
            // Show the error messages to the client before the server informs.
            // We know the client did an error because of the state machine
            // and we want to inform the client before he needs to read a server 
            // mesage in order to tell him what happens right now
            FANCY_CLIENT("PROTOCOL ERROR.\n", Format.BOLD, Format.RED);
            FANCY_CLIENT("Oh! It looks like you did a ", Format.RED);
            FANCY_CLIENT("protocol error", Format.UNDERLINE, Format.RED);
            FANCY_CLIENT(". The action " + action.name() + " can't be applied now. Please try again.\n", Format.RED);
        }
    }

    public List<Action> getValidActions() {
        List<Action> validActions = new ArrayList();
        validActions.addAll(state.transitions.keySet());
        validActions.remove(Action.SHOW);
        return validActions;
    }
    
    // Fill the transitions map
    static {
        for (State state : State.values())
            switch (state) {

                case INIT:
                    state.transitions.put(Action.START, State.START);
                    break;

                case START:
                    state.transitions.put(Action.ANTE_STAKES, State.ACCEPT_ANTE);
                    break;
                    
                case ACCEPT_ANTE:
                    state.transitions.put(Action.ANTE_OK, State.PLAY);
                    state.transitions.put(Action.QUIT, State.QUIT);
                    break;
                    
                case PLAY:
                    state.transitions.put(Action.DEALER_HAND, State.BETTING);
                    break;
                    
                case BETTING:
                    state.transitions.put(Action.PASS, State.BETTING_DEALER);
                    state.transitions.put(Action.BET, State.COUNTER);
                    break;
                    
                case BETTING_DEALER:
                    state.transitions.put(Action.PASS, State.DRAW);
                    state.transitions.put(Action.BET, State.COUNTER);
                    // 2nd time betting, exits to showdown
                    state.transitions.put(Action.SHOW, State.SHOWDOWN);
                    break;
                    
                case COUNTER:
                    state.transitions.put(Action.CALL, State.DRAW);
                    state.transitions.put(Action.RAISE, State.COUNTER);
                    state.transitions.put(Action.FOLD, State.SHOWDOWN); // TODO to know: edited State.QUIT to SHOWDOWN
                    // 2nd time betting, exits to showdown
                    state.transitions.put(Action.SHOW, State.SHOWDOWN);
                    break;
                    
                case DRAW:
                    state.transitions.put(Action.DRAW, State.DRAW_SERVER);
                    break;
                    
                case DRAW_SERVER:
                    state.transitions.put(Action.DRAW_SERVER, State.BETTING);
                    break;
                    
                case SHOWDOWN:
                    state.transitions.put(Action.STAKES, State.ACCEPT_ANTE);
                    break;
            }
    }    
}
