package poker5cardgame.ai;

import java.util.ArrayList;
import java.util.List;
import poker5cardgame.game.Card;
import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.game.GameState.Action;
import poker5cardgame.game.Move;

public class RandomClientAI extends ArtificialIntelligence {

    // Fix the max bet to 500 to be realistic
    private static final int MAX_BET = 500;

    public RandomClientAI(GameData gameData, GameState gameState) {
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

    /**
     * Get a random action for the client. Avoid the case QUIT.
     *
     * @param gameData
     * @param gameState
     * @return GameState.Action
     */
    public GameState.Action randomAction(GameData gameData, GameState gameState) {
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

    @Override
    public Move getNextMove() {

        Move cMove = new Move();
        cMove.action = randomAction(gameData, gameState);

        // Set the required move parameters
        switch (cMove.action) {
            case START:
                // Set a random client id between 1 and 1000
                cMove.id = random(1, 1000);
                break;

            case BET:
                // Set a random bet if it is possible, if not bet the minimum bet
                int bet = random(gameData.minBet, MAX_BET);
                if (gameData.cChips >= bet) {
                    cMove.chips = bet;
                } else {
                    cMove.chips = random(gameData.minBet, gameData.cChips);
                }
                break;

            case RAISE:
                // Set a random raise if it is possible, if not raise the minimum bet
                int raise = random(1, MAX_BET);
                if (gameData.cChips >= raise) {
                    cMove.chips = raise;
                } else {
                    cMove.chips = random(1, gameData.cChips);
                }
                break;

            case DRAW:
                // Discard the first x random cards
                cMove.cDrawn = random(0, 5);
                cMove.cards = new Card[cMove.cDrawn];
                for (int i = 0; i < cMove.cDrawn; i++) {
                    cMove.cards[i] = gameData.cHand.getCards().get(i);
                }
                break;
        }
        return cMove;
    }
    
    @Deprecated
    @Override
    public boolean sendMove(Move move) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
