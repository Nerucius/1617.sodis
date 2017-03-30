package poker5cardgame.network;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import poker5cardgame.ai.ArtificialIntelligence;
import poker5cardgame.game.GameData;
import poker5cardgame.game.ServerGame;
import poker5cardgame.game.GameState;
import poker5cardgame.io.NetworkSource;
import static poker5cardgame.Log.*;

/**
 *
 */
public class MTGameServer extends MultithreadServer {

    Map<Integer, GameData> savedGames = new ConcurrentHashMap<>();

    ArtificialIntelligence.Type AIType = ArtificialIntelligence.Type.AI_RANDOM;

    @Override
    public void handleConnection(Socket sock) {
        NetworkSource source = new NetworkSource(sock);
        
        // NOTE: Timeout Disabled Enable here
        source.getCom().setTimeout(0);

        ServerGame game = new ServerGame(source, AIType);
        int gameID = -1;

        while (game.getState() != GameState.State.QUIT) {
            game.update();
            
            // Intercept ID, restore data if applicable
            if (game.getState() == GameState.State.START) {
                gameID = game.getID();
                loadGameData(game, gameID);
            }
            

        }

        // Commit new GameData to memory
        GAME_DEBUG(gameID, "Saved Game data");
        savedGames.put(gameID, game.getGameData());
    }

    private void loadGameData(ServerGame game, int gameID) {
        if (savedGames.containsKey(gameID)) {
            GAME_DEBUG(gameID, "Loaded previous data for Game");
            GameData prevData = savedGames.get(gameID);
            game.copyGameData(prevData);
        }
    }

    public void setAIType(ArtificialIntelligence.Type type) {
        this.AIType = type;
    }

}
