package poker5cardgame.ai;

import java.util.ArrayList;
import java.util.List;
import poker5cardgame.game.Card;
import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.game.GameState.Action;
import poker5cardgame.game.Move;

import static poker5cardgame.Log.*;

public class RandomClientAI extends ArtificialIntelligence {

    // Fix the max bet to 500 to be realistic
    private static final int MAX_BET = 500;

    public RandomClientAI(GameData gameData, GameState gameState) {
        super(gameData, gameState);
    }

    @Override   
    public Move getNextMove() {
        Move move = new Move();
        move.action = randomAction();

        AI_TRACE("CAI: Requested move for " + gameState.state);

        // Set the required move parameters
        switch (move.action) {
            case START:
                // Set a random client id between 1 and 1000
                move.id = random(1, 1000);
                break;

            case BET:
                // Set a random bet if it is possible, if not bet the minimum bet
                int bet = random(gameData.minBet, MAX_BET);
                if (gameData.cChips >= bet) {
                    move.chips = bet;
                } else {
                    move.chips = random(gameData.minBet, gameData.cChips);
                }
                break;

            case RAISE:
                // Set a random raise if it is possible, if not raise the minimum bet
                int raise = random(1, MAX_BET);
                if (gameData.cChips >= raise) {
                    move.chips = raise;
                } else {
                    move.chips = random(1, gameData.cChips);
                }
                break;

            case DRAW:
                // Discard the first x random cards
                move.cDrawn = random(0, 5);
                move.cards = new Card[move.cDrawn];
                for (int i = 0; i < move.cDrawn; i++) {
                    move.cards[i] = gameData.cHand.getCards().get(i);
                }
                break;
        }

        AI_DEBUG("CAI: Sent: " + move);
        
        // Save move to our own recording of the Game
        sendMove(move);

        return move;
    }

    @Override
    public boolean sendMove(Move move) {
        AI_DEBUG("CAI: Saved: " + move);
        gameState.apply(move.action);
        gameData.save(move);
        return true;
    }

    /**
     * Get a random action for the client. Avoid the case QUIT.
     *
     * @return GameState.Action
     */
    private GameState.Action randomAction() {
        List<Action> validActions = new ArrayList();
        validActions.addAll(gameState.state.transitions.keySet());

        // Avoid the option QUIT
        if (validActions.contains(GameState.Action.QUIT)) {
            validActions.remove(GameState.Action.QUIT);
        }

        // The case SHOW can not be choosen, it is automatically applied in the apply method
        if (validActions.contains(Action.SHOW)) {
            validActions.remove(Action.SHOW);
        }

        return validActions.get(random(0, validActions.size() - 1));
    }

    /**
     * Get a random number between min and max.
     */
    private int random(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }
}
