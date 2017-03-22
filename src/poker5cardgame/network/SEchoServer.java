package poker5cardgame.network;

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
            System.out.println("Server: Received " + rawData);
            
            event.server.send(event.socket, event.data);
            
        }
        
    }
}
