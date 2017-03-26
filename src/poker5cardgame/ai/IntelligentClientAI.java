package poker5cardgame.ai;

import java.util.List;
import static poker5cardgame.Log.AI_DEBUG;
import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.game.Move;

public class IntelligentClientAI extends ArtificialIntelligence {

    public IntelligentClientAI(GameData gameData, GameState gameState) {
        super(gameData, gameState);
    }
    
    @Override
    public Move getNextMove() {
        
        Move move = new Move();
        
        switch (gameState.state)
        {
            case INIT:
                move.id = random(1, 1000);
                break;
                
            case BETTING:
                return betting(client);
                
            case BETTING_DEALER:                
                return betting(client);            
                
            case COUNTER:
                return counting(client);
                
            case DRAW:
                return drawing(client);
        }        
        
        List<GameState.Action> validActions = validActions(server);
        move.action = validActions.get(0);
        return move;
    }

    @Override
    public boolean sendMove(Move move) {
        AI_DEBUG("CAI: Saved: " + move);
        gameData.save(move, gameState.isServerTurn());
        return true;
    }
}
