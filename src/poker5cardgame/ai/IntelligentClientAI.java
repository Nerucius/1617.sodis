package poker5cardgame.ai;

import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.game.Move;

// TODO @sonia
public class IntelligentClientAI extends ArtificialIntelligence {

    public IntelligentClientAI(GameData gameData, GameState gameState) {
        super(gameData, gameState);
    }
    
    @Override
    public Move getNextMove() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Deprecated
    @Override
    public boolean sendMove(Move move) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
