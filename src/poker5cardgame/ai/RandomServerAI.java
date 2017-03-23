package poker5cardgame.ai;

import java.util.ArrayList;
import java.util.List;
import poker5cardgame.game.Card;
import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.game.GameState.Action;
import poker5cardgame.game.Move;

import static poker5cardgame.Log.*;

public class RandomServerAI extends ArtificialIntelligence {

    // Fix the max bet to 500 to be realistic
    private static final int MAX_BET = 500;

    public RandomServerAI(GameData gameData, GameState gameState) {
        super(gameData, gameState);
    }

    /**
     * Get a random number between min and max.
     *
     * @param min int
     * @param max int
     * @return int
     */
    private int random(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }

    @Override
    public Move getNextMove() {

        Move move = new Move();
        move.action = randomAction();

        // set the required move parameters
        switch (move.action) {
            case BET:
                // Set a random bet if it is possible, if not bet the minimum bet
                int bet = random(gameData.minBet, MAX_BET);
                if (gameData.sChips >= bet) {
                    move.chips = bet;
                } else {
                    move.chips = random(gameData.minBet, gameData.sChips);
                }
                break;

            case RAISE:
                // Set a random raise if it is possible, if not raise the minimum bet
                int raise = random(1, MAX_BET);
                if (gameData.sChips >= raise) {
                    move.chips = raise;
                } else {
                    move.chips = random(1, gameData.sChips);
                }
                break;

            case DRAW_SERVER:
                // Discard the first x random cards
                move.sDrawn = random(0, 5);
                Card[] cardsToDiscard = new Card[move.sDrawn];
                for (int i = 0; i < move.sDrawn; i++) {
                    cardsToDiscard[i] = gameData.sHand.getCards().get(i);
                }
                gameData.sHand.discard(cardsToDiscard);
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

    private GameState.Action randomAction() {
        List<Action> validActions = new ArrayList();
        validActions.addAll(gameState.state.transitions.keySet());

        // The case SHOW can not be choosen, it is automatically applied in the apply method
        if (validActions.contains(Action.SHOW)) {
            validActions.remove(Action.SHOW);
        }

        return validActions.get(random(0, validActions.size() - 1));
    }
}
