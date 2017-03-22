package poker5cardgame.network;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import static poker5cardgame.Log.*;

public abstract class SelectorWorker implements Runnable {

    protected final List queue = new LinkedList();

    public abstract void handleData(ServerDataEvent data);

    /**
     * Insert an incoming data stream onto our processing queue. Copy
     * data and wake up sleeping Threads.
     */
    public final void processData(SelectorServer server, SocketChannel socket, byte[] data, int count) {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        synchronized (queue) {
            queue.add(new ServerDataEvent(server, socket, dataCopy));
            queue.notify();
        }
    }

    @Override
    public final void run() {
        ServerDataEvent dataEvent;
        try {

            while (true) {
                // Wait for data to become available
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        // Block this thread while the server is not receiving
                        // any new data streams.
                        queue.wait();
                    }
                    dataEvent = (ServerDataEvent) queue.remove(0);
                }

                // Delegate to abstract function
                this.handleData(dataEvent);
            }
            
        } catch (InterruptedException e) {
            NET_ERROR("Worker: Thread interrupted. Worker stopped.");
            e.printStackTrace();
        }
    }

    /**
     * Internal class to our Worker to encapsulate a new Data Event received by
     * the server on a given Socket.
     */
    public static class ServerDataEvent {

        public SelectorServer server;
        public SocketChannel socket;
        public byte[] data;

        public ServerDataEvent(SelectorServer server, SocketChannel socket, byte[] data) {
            this.server = server;
            this.socket = socket;
            this.data = data;
        }
    }
}
