
package poker5cardgame.ai;

import java.util.List;
import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.game.Move;

public class IntelligentServerAI extends ArtificialIntelligence {

    public IntelligentServerAI(GameData gameData, GameState gameState) {
        super(gameData, gameState);
    }

    @Override
    public Move getNextMove() {       
       
        switch(gameState.state)
        {
            case BETTING:
                return betting(server);
                
            case BETTING_DEALER:                
                return betting(server);            
                
            case COUNTER:
                return counting(server);
                
            case DRAW_SERVER:
                return drawing(server);
        }
        
        Move move = new Move();
        List<GameState.Action> validActions = validActions(server);
        move.action = validActions.get(0);
        return move;
    }

    @Deprecated
    @Override
    public boolean sendMove(Move move) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}