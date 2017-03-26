package poker5cardgame.ai;

import java.util.ArrayList;
import java.util.List;
import static poker5cardgame.Log.*;
import poker5cardgame.game.Card;
import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.game.Hand;
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
        // Do that only if we are in a betting round
        if(!validActions.contains(GameState.Action.PASS) && !validActions.contains(GameState.Action.CALL))
            return;
        
        if(server)
        {
            boolean client = !server;
            // If the client has done an all in, the server should CALL (if it's possible)
            if(allIn(client) && validActions.contains(GameState.Action.CALL))
            {
                if(possibleCall(gameData, server))   
                {
                    validActions.clear();
                    validActions.add(GameState.Action.CALL);
                    return;
                }
                else
                {
                    validActions.clear();
                    validActions.add(GameState.Action.FOLD);
                    return;
                }
            }
            // Manage if the server can BET
            if(maxBet(server) == 0)
                validActions.remove(GameState.Action.BET);
            // Manage if the server can RAISE
            if(maxRaise(server) == 0)
                validActions.remove(GameState.Action.RAISE);
            // Manage if the server can CALL
            if(!possibleCall(gameData, server))
                validActions.remove(GameState.Action.CALL);
        }
        else
        {            
            boolean client = server;
            // If the server has done an all in, the client should CALL (if it's possible)
            if(allIn(server) && validActions.contains(GameState.Action.CALL))
            {
                if(possibleCall(gameData, server))   
                {
                    validActions.clear();
                    validActions.add(GameState.Action.CALL);
                    return;
                }
                else
                {
                    validActions.clear();
                    validActions.add(GameState.Action.FOLD);
                    return;
                }
            }
            // Manage if the client can BET
            if(maxBet(client) == 0)
                validActions.remove(GameState.Action.BET);
            // Manage if the client can RAISE
            if(maxRaise(client) == 0)
                validActions.remove(GameState.Action.RAISE);
            // Manage if the client can CALL
            if(!possibleCall(gameData, client))
                validActions.remove(GameState.Action.CALL);        
        }
        AI_DEBUG("Valid actions after control betting: " + validActions);
    }
    protected void manageBettingPossibilities2(List validActions, boolean server)
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
            } else if (allIn(server)) {
                validActions.remove(GameState.Action.BET);
                validActions.remove(GameState.Action.RAISE);
                validActions.remove(GameState.Action.CALL);
            }else if(maxRaise(server) == 0)
                validActions.remove(GameState.Action.RAISE);
            else if(maxBet(server) == 0)
                validActions.remove(GameState.Action.BET);
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
            } else if (allIn(client)) {
                validActions.remove(GameState.Action.BET);
                validActions.remove(GameState.Action.RAISE);
                validActions.remove(GameState.Action.CALL);
            } else if(maxRaise(client) == 0)
                validActions.remove(GameState.Action.RAISE);
            else if(maxBet(client) == 0)
                validActions.remove(GameState.Action.BET);
        }
    }
    
    protected int maxBet(boolean server)
    {
        int maxBet;
        if(server)
        {
            maxBet = gameData.sChips;
            if(gameData.cChips < gameData.sChips)
                maxBet = gameData.cChips;
        }
        else
        {
            maxBet =gameData.cChips;
            if(gameData.sChips < gameData.cChips)
                maxBet = gameData.sChips;
        }
        return maxBet;
    }
    
    protected int maxRaise(boolean server)
    {
        int maxRaise;
        if(server)
        {
            maxRaise = maxBet(server) - gameData.cBet;
            if(!possibleRaise(gameData, maxRaise, server))
            {
                maxRaise = 0;
                AI_DEBUG("NOT POSSIBLE RAISE SERVER");
            }
           
        }
        else
        {
            boolean client = server;
            maxRaise = maxBet(client) - gameData.sBet;
            if(!possibleRaise(gameData, maxRaise, client))
            {
                maxRaise = 0;
                AI_DEBUG("NOT POSSIBLE RAISE CLIENT");
            }
        }
        return maxRaise;
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
        if(chips < 0)
            return false;
        return (server && gameData.sChips >= chips) || (!server && gameData.cChips >= chips);
    }
    
    public static boolean possibleRaise(GameData gameData, int chips, boolean server) {
        if(chips < 0)
            return false;
        
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
    
    public static int manageWinner(GameData gameData)
    {
        Card[] sCards = new Card[Hand.SIZE];
        gameData.sHand.getCards().toArray(sCards);

        Card[] cCards = new Card[Hand.SIZE];
        gameData.cHand.getCards().toArray(cCards);
        
        Hand sRankerHand = new Hand(sCards);
        sRankerHand.generateRankerInformation();
        
        Hand cRankerHand = new Hand(cCards);
        cRankerHand.generateRankerInformation();
        
        AI_DEBUG("manageWinner: sRankerHand: " + sRankerHand);
        AI_DEBUG("manageWinner: cRankerHand: " + cRankerHand);
        
        if(sRankerHand.wins(cRankerHand))
            return 0;
        if(sRankerHand.loses(cRankerHand))
            return 1;
        if(sRankerHand.ties(cRankerHand))
            return 2;
        
        AI_DEBUG("manageWinner after: sRankerHand: " + sRankerHand);
        AI_DEBUG("manageWinner after: cRankerHand: " + cRankerHand);
        AI_DEBUG("manageWinner after: sHand: " + gameData.sHand);
        AI_DEBUG("manageWinner after: cHand: " + gameData.cHand);
        
        return -1;
    }
}
