
package poker5cardgame.ai;

import poker5cardgame.game.GameData;
import poker5cardgame.game.GameState;
import poker5cardgame.game.Hand;
import poker5cardgame.game.HandRanker;
import poker5cardgame.game.HandRanker.HandRank;
import poker5cardgame.game.Move;

// TODO @sonia change it, now it is random
public class IntelligentServerAI extends ArtificialIntelligence {

    public IntelligentServerAI(GameData gameData, GameState gameState) {
        super(gameData, gameState);
    }

    @Override
    public Move getNextMove() {

        Move sMove = new Move();
        switch(gameState.state)
        {
            case BETTING:
                this.betting(gameData);
                break;
                
            case BETTING_DEALER:
                this.betting(gameData);
                break;
                
            case COUNTER:
                break;
                
            case DRAW_SERVER:
                break;
        }
        return sMove;
    }
    
    private void betting(GameData gameData) {
        gameData.sHand.generateRankerInformation();
        HandRank sHandRank = HandRanker.getHandRank(gameData.sHand);
        System.out.println("DEBUG BETTING AI sHandRank: " + sHandRank);
        System.out.println("DEBUG BETTING AI ocur dict: " + gameData.sHand.getOcurDict());
        System.out.println("DEBUG BETTING AI rank dict: " + gameData.sHand.getRankDict());
        System.out.println("DEBUG BETTING AI suit id: " + gameData.sHand.getSuitId());
        System.out.println("DEBUG BETTING AI weight: " + gameData.sHand.getWeight());
        System.out.println("DEBUG BETTING AI highest card: " + gameData.sHand.getHigh(gameData.sHand.getCards()));
   
        int leftoverCards = Hand.getKeysByValue(gameData.sHand.getOcurDict(), 1).size();
        System.out.println("DEBUG BETTING AI cartes sueltes: " + leftoverCards);

        
    }
    
    @Deprecated
    @Override
    public boolean sendMove(Move move) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}