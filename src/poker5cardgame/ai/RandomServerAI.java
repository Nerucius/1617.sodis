package poker5cardgame.ai;

import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import poker5cardgame.game.Card;
import poker5cardgame.game.Deck;
import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.game.GameState.Action;
import poker5cardgame.game.Move;

public class RandomServerAI implements ArtificialIntelligence {

    private int random(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }
    
    public GameState.Action randomAction(GameState gameState) {
        Set validActions = gameState.state.transitions.keySet();
        if (validActions.contains(Action.SHOW)) validActions.remove(Action.SHOW);
        return (Action) validActions.toArray()[random(0, validActions.size() - 1)];
    }
    
    @Override
    public Move getMoveForGame(GameData gameData, GameState gameState) throws Exception {
        
        Move sMove = new Move();
        sMove.action = randomAction(gameState);
        
        // set the required move parameters
        switch(sMove.action)
        {
            case BET:       
                int bet = random(100,500);
                if(gameData.sChips >= bet)                    
                    sMove.chips = bet;  
                else
                    sMove.chips = random(gameData.minBet, gameData.sChips);
                break;
                
            case RAISE:
                int raise = random(1,500);
                if(gameData.sChips >= raise)                    
                    sMove.chips = raise;  
                else
                    sMove.chips = Math.round(random(1, gameData.sChips));
                break;
                
            case DRAW_SERVER:
                sMove.sDrawn = random(0,5);
                for (int i = 0; i < sMove.sDrawn; i++) {
                    int randomIdx = random(0, gameData.sHand.getSize() - 1);
                    gameData.sHand.discard(gameData.sHand.getCards().get(randomIdx));
                }
                gameData.sHand.putNCards(gameData.deck, sMove.sDrawn);
                break;
        }
        
        return sMove;
    }   
}