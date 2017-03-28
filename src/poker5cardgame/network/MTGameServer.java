package poker5cardgame.network;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import poker5cardgame.ai.ArtificialIntelligence;
import poker5cardgame.game.ServerGame;
import poker5cardgame.game.GameState;
import poker5cardgame.io.NetworkSource;

/**
 * 
 */
public class MTGameServer extends MultithreadServer{
    
    Map<Integer, ServerGame> savedGames = new ConcurrentHashMap<>();
    
    ArtificialIntelligence.Type AIType = ArtificialIntelligence.Type.AI_RANDOM;

    @Override
    public void handleConnection(Socket sock) {
        NetworkSource source = new NetworkSource(sock);
        // TODO @alex 
        source.getCom().setTimeout(0); 
        
        ServerGame game = new ServerGame(source, AIType);
        int id = 0;
        // TODO @alex Add persistence
        /*int id = game.getPlayerID();
        if(savedGames.containsKey(id)){
            game = savedGames.get(id);
        }
        */
        
        while(game.getState() != GameState.State.QUIT){
            game.update();
        }
        
        savedGames.put(id, game);
    }
    
    public void setAIType(ArtificialIntelligence.Type type){
        this.AIType = type;
    }
    
}
