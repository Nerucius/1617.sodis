package poker5cardgame.ai;

import poker5cardgame.game.Move;
import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.io.Source;

public abstract class ArtificialIntelligence implements Source {
    
    protected GameData gameData;
    protected GameState gameState;
    
    protected ArtificialIntelligence(GameData gameData, GameState gameState)
    {
        this.gameData = gameData;
        this.gameState = gameState;
    }
    
    // TODO @sonia Consider whether the client and server can use the same AI
    //public Move getMoveForGame(GameData gameData, GameState gameState);
}
