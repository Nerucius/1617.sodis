package poker5cardgame.ai;

import java.util.ArrayList;
import java.util.List;
import static poker5cardgame.Log.*;
import poker5cardgame.game.Card;
import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.game.Hand;
import poker5cardgame.game.HandRanker;
import poker5cardgame.game.Move;
import poker5cardgame.io.Source;

public abstract class ArtificialIntelligence implements Source {

    protected final boolean server = true;
    protected final boolean client = false;

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
    
    protected Move bet(int chips, boolean server)
    {
        List<GameState.Action> validActions = validActions(server);
        Move move = new Move();
        
        if (!validActions.contains(GameState.Action.BET)) {
            move.action = GameState.Action.PASS;
            return move;
        }

        int MAX_BET = maxBet(server);
        move.action = GameState.Action.BET;
        move.chips = chips;
        if (move.chips > MAX_BET) {
            move.chips = MAX_BET;
        }

        return move;   
    }
    
    protected Move raise(int chips, boolean server)
    {
        List<GameState.Action> validActions = validActions(server);
        Move move = new Move();
        
        if (!validActions.contains(GameState.Action.RAISE)) {
            if (possibleCall(gameData, server)) {
                move.action = GameState.Action.CALL;
            } else {
                move.action = GameState.Action.FOLD;
            }
            return move;
        }
        
        int MAX_RAISE = maxRaise(server);
        move.action = GameState.Action.RAISE;
        move.chips = chips;
        if (move.chips > MAX_RAISE) {
            move.chips = MAX_RAISE;
        }
        
        return move;
    }
    
    protected Move betting(boolean server) {
        
        List<GameState.Action> validActions = validActions(server);
        Move move = new Move();
        HandRanker.HandRank handRank;
        
        if (server) {
            gameData.sHand.generateRankerInformation();
            handRank = HandRanker.getHandRank(gameData.sHand);
        } else {
            gameData.cHand.generateRankerInformation();
            handRank = HandRanker.getHandRank(gameData.cHand);
        }
        
        if (!validActions.contains(GameState.Action.BET) || handRank == HandRanker.HandRank.HIGH_CARD) {
            move.action = GameState.Action.PASS;
            return move;
        }
        
        switch(handRank)
        {   
            case ONE_PAIR:
                return bet(100, server);
                
            case TWO_PAIR:
                return bet(200, server);
                
            case THREE_OF_A_KIND:
                return bet(500, server);
                
            default:
                if(server)
                    return bet(gameData.sChips, server);
                else
                    return bet(gameData.cChips, server);
        }
    }
    
    protected Move countering(boolean server)
    {
        Move move = new Move();
        HandRanker.HandRank handRank;
        
        if (server) {
            gameData.sHand.generateRankerInformation();
            handRank = HandRanker.getHandRank(gameData.sHand);
        } else {
            gameData.cHand.generateRankerInformation();
            handRank = HandRanker.getHandRank(gameData.cHand);
        }

        switch (handRank) {
            case HIGH_CARD:
                if(possibleCall(gameData, server))
                    move.action = GameState.Action.CALL;
                else
                    move.action = GameState.Action.FOLD;
                break;

            case ONE_PAIR:
                return raise(50, server);

            case TWO_PAIR:
                return raise(200, server);
                
            case THREE_OF_A_KIND:                
                return raise(500, server);
                
            default:
                if(server)
                    return raise(gameData.sChips, server);
                else
                    return raise(gameData.cChips, server);
        }
        return move;
    }
    
    protected Move drawing(boolean server) {
        
        Move move = new Move();
        int drawn = -1;
        
        HandRanker.HandRank handRank;
        
        if (server) {
            gameData.sHand.generateRankerInformation();
            handRank = HandRanker.getHandRank(gameData.sHand);
        } else {
            gameData.cHand.generateRankerInformation();
            handRank = HandRanker.getHandRank(gameData.cHand);
        }

        Card[] cardsToDiscard = new Card[0];
        AI_DEBUG("drawing() "+handRank);
        switch(handRank)
        {
            case HIGH_CARD:
                drawn = 5;
                break;
                 
            case ONE_PAIR:
                drawn = 3;
                break;
                
            case TWO_PAIR:
                drawn = 1;
                break;
                
            case THREE_OF_A_KIND:
                drawn = 2;
                break;
                
            case STRAIGHT:
                drawn = 0;
                break;

            case FLUSH:
                drawn = 0;
                break;

            case FULL_HOUSE:
                drawn = 0;
                break;
                
            case FOUR_OF_A_KIND:
                drawn = 1;
                break;
                
            case STRAIGHT_FLUSH:
                drawn = 0;
                break;               
            
        }
        if(drawn > 0)
        {
            cardsToDiscard = discardLeftoverCards(drawn, server);
            if (server) {
                try {
                    gameData.sHand.discard(cardsToDiscard);
                } catch (Exception ex) {/* Ignored. This exception will not be thrown because the ai discard the right cards */
                }
            }             
        }
        
        move.cards = cardsToDiscard;
        if(server)
        {
            move.action = GameState.Action.DRAW_SERVER;
            move.sDrawn = drawn;
        }
        else
        {   
            move.action = GameState.Action.DRAW;
            move.cDrawn = drawn;
        }
        
        return move;
    }
    
    private Card[] discardLeftoverCards(int drawn, boolean server)
    {        
        AI_DEBUG("discardLeftovers: " + drawn);
        List<Card> toDiscardList;
        List<Card.Rank> toDiscardRanks;
        
        if (server) {
            toDiscardList = new ArrayList(gameData.sHand.getCards());
            toDiscardRanks = Hand.getKeysByValue(gameData.sHand.getOcurDict(), 1);
        } else {
            toDiscardList = new ArrayList(gameData.cHand.getCards());
            toDiscardRanks = Hand.getKeysByValue(gameData.cHand.getOcurDict(), 1);
        }

        List<Card> aux = new ArrayList(toDiscardList);
        for(Card cardToDiscard : aux)
            if(!toDiscardRanks.contains(cardToDiscard.getRank()))
                toDiscardList.remove(cardToDiscard);
          
        Card[] cardsToDiscard = new Card[drawn];
        toDiscardList.toArray(cardsToDiscard);
        return cardsToDiscard;
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
