package poker5cardgame.ai;

import java.util.ArrayList;
import java.util.List;
import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.io.Source;

public abstract class ArtificialIntelligence implements Source {

    protected GameData gameData;
    protected GameState gameState;

    public enum Type {
        AI_RANDOM(1),
        AI_INTELLIGENT(2);
        int code;

        Type(int code) {
            this.code = code;
        }
        
        static public Type fromCode(int code){
            for(Type t : values())
                if (t.code == code)
                    return t;
            return null;
        }
    }

    protected ArtificialIntelligence(GameData gameData, GameState gameState) {
        this.gameData = gameData;
        this.gameState = gameState;
    }

    /**
     * Get a random number between min and max.
     * @param min
     * @param max
     */
    protected int random(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }
    
    /**
     * Get a random valid action.
     *
     * @param server
     * @return GameState.Action
     */
    protected GameState.Action randomAction(boolean server) {        
        List<GameState.Action> validActions = validActions(server);
        return validActions.get(random(0, validActions.size() - 1));
    }
    
    /**
     * Get the list of valid actions. Avoid the case QUIT.
     *
     * @param server
     * @return List of GameState.Action
     */
    protected List<GameState.Action> validActions(boolean server) {
        List<GameState.Action> validActions = new ArrayList();
        validActions.addAll(gameState.state.transitions.keySet());

        // Avoid the option QUIT
        validActions.remove(GameState.Action.QUIT);       
        // The case SHOW can not be choosen, it is automatically applied in the apply method
        validActions.remove(GameState.Action.SHOW);        
        manageBettingPossibilities(validActions, server);
        
        return validActions;
    }
    
    protected void manageBettingPossibilities(List validActions, boolean server)
    {
        if (server) {
            boolean client = !server;
            if (allIn(client)) {
                validActions.remove(GameState.Action.BET);
                validActions.remove(GameState.Action.RAISE);
                // If the client has done an ALL IN, the server should CALL
                if(validActions.contains(GameState.Action.CALL))
                {
                    validActions.clear();
                    validActions.add(GameState.Action.CALL);
                }
            } else if(!possibleCall(gameData, server)) {
                validActions.remove(GameState.Action.RAISE);
                validActions.remove(GameState.Action.CALL); 
            }
        }
        else {
            boolean client = server;
            if (allIn(server)) {
                validActions.remove(GameState.Action.BET);
                validActions.remove(GameState.Action.RAISE);
                // If the server has done an ALL IN, the client should CALL
                if(validActions.contains(GameState.Action.CALL))
                {
                    validActions.clear();
                    validActions.add(GameState.Action.CALL);
                }                      
            } else if(!possibleCall(gameData, client)) {
                validActions.remove(GameState.Action.RAISE);
                validActions.remove(GameState.Action.CALL); 
            }
        }
    }
    
    protected boolean allIn(boolean server)
    {
        if(server)
            return gameData.sChips == 0;
        else
            return gameData.cChips == 0;
    }
    
    public static boolean possibleNewRound(GameData gameData) {
        return (gameData.sChips >= gameData.initialBet && gameData.cChips >= gameData.initialBet);
    }
    
    public static boolean possibleBet(GameData gameData, int chips, boolean server) {        
        return (server && gameData.sChips >= chips) || (!server && gameData.cChips >= chips);
    }
    
    public static boolean possibleRaise(GameData gameData, int chips, boolean server) {
        if(server)
        {
            int amount = gameData.cBet - gameData.sBet + chips;
            return possibleBet(gameData, amount, server);
        }
        else
        {
            boolean client = server;
            int amount = gameData.sBet - gameData.cBet + chips;
            return possibleBet(gameData, amount, client);
        }
    }
    
    public static boolean possibleCall(GameData gameData, boolean server) {
        return possibleRaise(gameData, 0, server);
    }
}
