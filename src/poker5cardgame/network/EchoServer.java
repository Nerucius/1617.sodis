/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.network;

import java.net.Socket;
import poker5cardgame.game.Game;
import poker5cardgame.io.NetworkSource;

/**
 * Echoing server. Listens for any correctly formatted protocol message and sends back
 * an ERRO message with the parsed command and arguments.
 *
 * @author German Dempere
 */
public class EchoServer extends MultithreadServer {
    
    NetworkSource source;

    @Override
    public void handleConnection(Socket client) {

        source = new NetworkSource(client);

        // Main Server <-> Client Loop
        while (true) {

            Game.Move m = source.getNextMove();
            if (m.action == Game.Action.TERMINATE) {
                System.out.println("Server: Received TERMINATE packet.");
                break;
            }
            System.out.println("Server: Echoing " + m.action);

            Game.Move echo = new Game.Move();
            echo.action = Game.Action.ERROR;
            echo.error = m.toString();
            source.sendMove(echo);
        }

    }

}
