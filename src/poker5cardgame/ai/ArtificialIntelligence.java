package poker5cardgame.ai;

import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.io.Source;

public abstract class ArtificialIntelligence implements Source {

    protected GameData gameData;
    protected GameState gameState;

    public enum Type {
        AI_RANDOM(1),
        AI_INTELLIGENT(2);
        int code;

        Type(int code) {
            this.code = code;
        }
        
        static public Type fromCode(int code){
            for(Type t : values())
                if (t.code == code)
                    return t;
            return null;
        }
    }

    protected ArtificialIntelligence(GameData gameData, GameState gameState) {
        this.gameData = gameData;
        this.gameState = gameState;
    }

    // TODO @sonia Consider whether the client and server can use the same AI
}
