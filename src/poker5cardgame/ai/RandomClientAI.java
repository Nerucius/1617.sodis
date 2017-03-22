package poker5cardgame.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import poker5cardgame.game.Card;
import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.game.GameState.Action;
import poker5cardgame.game.Move;

public class RandomClientAI implements ArtificialIntelligence {

    private static final int MAX_BET = 500;

    private int random(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }
    
    public GameState.Action randomAction(GameData gameData, GameState gameState) {
        List<Action> validActions = new ArrayList();
        validActions.addAll(gameState.state.transitions.keySet());
                
        // We avoid the option QUIT
        if (validActions.contains(GameState.Action.QUIT)) validActions.remove(GameState.Action.QUIT);
        
        // The case SHOW can not be choosen, it is automatically applied in the apply method
        if (validActions.contains(Action.SHOW)) validActions.remove(Action.SHOW);
        
        return validActions.get(random(0, validActions.size() -1 ));
    }
    
    @Override
    public Move getMoveForGame(GameData gameData, GameState gameState) /*throws Exception*/ {
        
        Move cMove = new Move();
        cMove.action = randomAction(gameData, gameState);

        // set the required move parameters
        switch(cMove.action)
        {
            case START:
                cMove.id = random(1,1000);
                break;
                                
            case BET:
                int bet = random(gameData.minBet,MAX_BET);
                if(gameData.cChips >= bet)                    
                    cMove.chips = bet;  
                else
                    cMove.chips = random(gameData.minBet, gameData.cChips);
                break;
                
            case RAISE:
                int raise = random(1,MAX_BET);
                if(gameData.cChips >= raise)                    
                    cMove.chips = raise;  
                else
                    cMove.chips = random(1, gameData.cChips);
                break;
                
            case DRAW:
                // Discard the first x random cards
                cMove.cDrawn = random(0,5);
                cMove.cards = new Card[cMove.cDrawn];
                for (int i = 0; i < cMove.cDrawn; i++) {
                    cMove.cards[i] = gameData.cHand.getCards().get(i);
                }
                break;
        }
        return cMove;
    }   

   
}
