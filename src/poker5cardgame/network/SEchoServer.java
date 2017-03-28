package poker5cardgame.network;

import static poker5cardgame.Log.*;
import poker5cardgame.ai.ArtificialIntelligence;

/**
 *
 * @author Akira
 */
public class SEchoServer extends SelectorServer {

    public SEchoServer() {
        this.worker = new EchoWorker();
    }

    private static class EchoWorker extends SelectorWorker {

        @Override
        public void handleData(ServerDataEvent event) {
            String rawData = new String(event.data);
            NET_TRACE("Server: Received " + rawData);

            event.server.send(event.socket, event.data);

        }

    }

    @Override
    public void setAIType(ArtificialIntelligence.Type type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
