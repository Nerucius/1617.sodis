package poker5cardgame.ai;

import poker5cardgame.game.Move;
import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;

public interface ArtificialIntelligence {

    // TODO @sonia Consider whether the client and server can use the same AI
    public Move getMoveForGame(GameData gameData, GameState gameState);
}
