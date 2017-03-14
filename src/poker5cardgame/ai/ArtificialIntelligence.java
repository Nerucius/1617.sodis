/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.ai;

import poker5cardgame.game.Game;
import poker5cardgame.game.Move;
import poker5cardgame.game.GameData;

/**
 *
 * @author sbatllpa9.alumnes
 */
public interface ArtificialIntelligence {

    // TODO @sonia Consider whether the client and server can use the same AI
    public Move getMoveForGame(GameData gData);
    
}
