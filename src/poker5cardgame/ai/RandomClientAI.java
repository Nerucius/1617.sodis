package poker5cardgame.ai;

import poker5cardgame.game.Card;
import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.game.Move;

import static poker5cardgame.Log.*;

public class RandomClientAI extends ArtificialIntelligence {

    private final boolean client = false;

    public RandomClientAI(GameData gameData, GameState gameState) {
        super(gameData, gameState);
    }

    @Override   
    public Move getNextMove() {
        
        int MAX_BET = gameData.cChips;
        if(gameData.sChips < gameData.cChips)
            MAX_BET = gameData.sChips;
       
        Move move = new Move();
        move.action = randomAction(client);

        AI_TRACE("CAI: Requested move for " + gameState.state);

        // Set the required move parameters
        switch (move.action) 
        {
            case START:
                move.id = random(1, 1000);
                break;

            case BET:
                move.chips = random(0, MAX_BET);             
                break;

            case RAISE:
                move.chips = random(1, MAX_BET);
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
        //sendMove(move);
        return move;
    }

    @Override
    public boolean sendMove(Move move) {
        AI_DEBUG("CAI: Saved: " + move);
        gameData.save(move, gameState.isServerTurn());
        return true;
    }
}
