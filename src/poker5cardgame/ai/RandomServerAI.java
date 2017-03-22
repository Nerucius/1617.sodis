package poker5cardgame.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import poker5cardgame.game.Card;
import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.game.GameState.Action;
import poker5cardgame.game.Move;

public class RandomServerAI implements ArtificialIntelligence {

    private static final int MAX_BET = 500;
    
    private int random(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }
    
    public GameState.Action randomAction(GameData gameData, GameState gameState) {
        List<Action> validActions = new ArrayList();
        validActions.addAll(gameState.state.transitions.keySet());  
        
        // The case SHOW can not be choosen, it is automatically applied in the apply method
        if (validActions.contains(Action.SHOW)) validActions.remove(Action.SHOW);
            
        return validActions.get(random(0, validActions.size() - 1));
    }
    
    @Override
    public Move getMoveForGame(GameData gameData, GameState gameState) /*throws Exception*/ {
        
        Move sMove = new Move();
        sMove.action = randomAction(gameData, gameState);  

        // set the required move parameters
        switch(sMove.action)
        {
            case BET:       
                int bet = random(gameData.minBet,MAX_BET);
                if(gameData.sChips >= bet)                    
                    sMove.chips = bet;  
                else
                    sMove.chips = random(gameData.minBet, gameData.sChips);
                break;
                
            case RAISE:
                int raise = random(1,MAX_BET);
                if(gameData.sChips >= raise)                    
                    sMove.chips = raise;  
                else
                    sMove.chips = random(1, gameData.sChips);
                break;
                
            case DRAW_SERVER:
                // Discard the first x random cards
                sMove.sDrawn = random(0,5);
                Card[] cardsToDiscard = new Card[sMove.sDrawn];
                for (int i = 0; i < sMove.sDrawn; i++) {
                    cardsToDiscard[i] = gameData.sHand.getCards().get(i);
                    //int randomIdx = random(0, gameData.sHand.getSize() - 1);
                    //gameData.sHand.discard(gameData.sHand.getCards().get(randomIdx));
                }
                gameData.sHand.discard(cardsToDiscard);
                //gameData.sHand.putNCards(gameData.deck, sMove.sDrawn);
                break;
        }
        
        return sMove;
    }   
}