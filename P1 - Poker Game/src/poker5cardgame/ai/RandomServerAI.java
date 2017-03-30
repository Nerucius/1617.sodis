package poker5cardgame.ai;

import poker5cardgame.game.Card;
import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.game.Move;

import static poker5cardgame.Log.*;

public class RandomServerAI extends ArtificialIntelligence {

    private final boolean server = true;

    public RandomServerAI(GameData gameData, GameState gameState) {
        super(gameData, gameState);
    }

    @Override
    public Move getNextMove() {
        
        int MAX_BET = maxBet(server);
        int MAX_RAISE = maxRaise(server);

        Move move = new Move();
        move.action = randomAction(server);

        // Set the required move parameters
        switch (move.action) 
        {
            case BET:
                move.chips = random(0, MAX_BET);             
                break;

            case RAISE:
                move.chips = random(1, MAX_RAISE);
                break;

            case DRAW_SERVER:
                // Discard the first x random cards
                move.sDrawn = random(0, 5);
                Card[] cardsToDiscard = new Card[move.sDrawn];
                for (int i = 0; i < move.sDrawn; i++) {
                    cardsToDiscard[i] = gameData.sHand.getCards().get(i);
                }            
                try {
                    gameData.sHand.discard(cardsToDiscard);
                } catch (Exception ex) {/* Ignored. This exception will not be thrown because the ai discard the right cards */}
                break;
        }

        AI_DEBUG("SAI: Sent move " + move);
        return move;
    }

    @Override
    public boolean sendMove(Move move) {
        AI_DEBUG("SAI: Recorded move " + move);
        return true;
    }
}
