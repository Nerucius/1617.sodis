package poker5cardgame.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author German Dempere
 */
public abstract class SelectorServer implements Server {

    private int port;
    // The channel on which we'll accept connections
    private ServerSocketChannel serverChannel;
    // The selector we'll be monitoring
    private Selector selector;
    // The buffer into which we'll read data when it's available
    private final ByteBuffer readBuffer = ByteBuffer.allocate(8192);

    // A list of ChangeRequest instances
    private final List changeRequests = new java.util.LinkedList();

    // Maps a SocketChannel to a list of ByteBuffer instances
    private final Map pendingData = new HashMap();

    protected SelectorWorker worker;

    @Override
    public void bind(int port) {
        this.port = port;
        this.selector = this.initSelector();
        if (this.selector == null)
            System.err.println("Server: Failed to Bind Selector.");
        else
            System.out.println("Server: Bound to port: " + port);
    }

    @Override
    public void start() {
        if (selector != null) {
            new Thread(this).start();
            new Thread(worker).start();
            System.out.println("Server: Started on port " + port);
        } else
            System.err.println("Server: Not bound to any port.");
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Process any pending changes
                synchronized (this.changeRequests) {
                    Iterator changes = this.changeRequests.iterator();
                    while (changes.hasNext()) {
                        ChangeRequest change = (ChangeRequest) changes.next();
                        switch (change.type) {
                            case ChangeRequest.CHANGEOPS:
                                SelectionKey key = change.socket.keyFor(this.selector);
                                key.interestOps(change.ops);
                        }
                    }
                    this.changeRequests.clear();
                }

                // Wait for an event one of the registered channels
                this.selector.select();

                // Iterate over the set of keys for which events are available
                Iterator selectedKeys = this.selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    // Check what event is available and deal with it
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) 
                        continue;                    

                    // New Connection
                    if (key.isAcceptable())
                        this.accept(key);
                    
                    // Incoming Data Stream
                    else if (key.isReadable())
                        this.read(key);
                    
                    // Outgoing Data Stream
                    else if (key.isWritable())
                        this.write(key);
                    
                }
            } catch (Exception e) {
                System.err.println("Server: Error Selecting Keys");
                e.printStackTrace();
                break;
            }
        }
        
        this.close();
    }

    /**
     * Send a stream of data to the given Socket. This method generates a new
     * Writable Key to be put on the send queue.
     *
     * @param socket
     * @param data
     */
    public void send(SocketChannel socket, byte[] data) {

        System.out.println("Server: send()");
        synchronized (this.changeRequests) {
            // Indicate we want the interest ops set changed
            this.changeRequests.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

            // And queue the data we want written
            synchronized (this.pendingData) {
                List queue = (List) this.pendingData.get(socket);
                if (queue == null) {
                    queue = new ArrayList();
                    this.pendingData.put(socket, queue);
                }
                System.out.println("Server: append to queue");
                queue.add(ByteBuffer.wrap(data));
            }
        }

        // Finally, wake up our selecting thread so it can make the required changes
        this.selector.wakeup();
    }

    @Override
    public void close() {
        try {
            selector.close();
            serverChannel.close();
            readBuffer.clear();

            // Clear queues
            pendingData.clear();
            changeRequests.clear();
        } catch (IOException ex) {
            System.err.println("Server: Error while closing down server.");
        }
    }

    /**
     * Initialize the Selector and bind to local:port.
     *
     * @return
     */
    private Selector initSelector() {
        Selector socketSelector;

        try {
            // Create a new selector
            socketSelector = SelectorProvider.provider().openSelector();

            // Create a new non-blocking server socket channel
            this.serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);

            // Bind the server socket to the specified address and port
            InetSocketAddress isa = new InetSocketAddress("localhost", this.port);
            serverChannel.socket().bind(isa);
            // Register the server socket channel, indicating an interest in 
            // accepting new connections
            serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return socketSelector;
    }

    /**
     * Method to Accept incoming Connection keys
     *
     * @param key
     * @throws IOException
     */
    private void accept(SelectionKey key) throws IOException {
        // For an accept to be pending the channel must be a server socket channel.
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // Accept the connection and make it non-blocking
        SocketChannel socketChannel = serverSocketChannel.accept();
        java.net.Socket socket = socketChannel.socket();
        socketChannel.configureBlocking(false);

        // Register the new SocketChannel with our Selector, indicating
        // we'd like to be notified when there's data waiting to be read
        socketChannel.register(this.selector, SelectionKey.OP_READ);
    }

    /**
     * Method to read an incoming Key.
     *
     * @param key
     * @throws IOException
     */
    protected void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        // Clear out our read buffer so it's ready for new data
        this.readBuffer.clear();

        // Attempt to read off the channel
        int numRead;
        try {
            numRead = socketChannel.read(this.readBuffer);
        } catch (IOException e) {
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            key.cancel();
            socketChannel.close();
            return;
        }

        if (numRead == -1) {
            // Remote entity shut the socket down cleanly. Do the
            // same from our end and cancel the channel.
            key.channel().close();
            key.cancel();
            return;
        }

        // Hand the data off to our worker thread
        //this.recievedData(this, socketChannel, this.readBuffer.array(), numRead);
        this.worker.processData(this, socketChannel, this.readBuffer.array(), numRead);
    }

    /**
     * Method to write data
     *
     * @param key
     * @throws IOException
     */
    protected void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        synchronized (this.pendingData) {
            List queue = (List) this.pendingData.get(socketChannel);

            // Write until there's not more data ...
            while (!queue.isEmpty()) {
                ByteBuffer buf = (ByteBuffer) queue.get(0);
                socketChannel.write(buf);
                if (buf.remaining() > 0) {
                    // ... or the socket's buffer fills up
                    break;
                }
                queue.remove(0);
            }

            if (queue.isEmpty()) {
                // We wrote away all data, so we're no longer interested
                // in writing on this socket. Switch back to waiting for
                // data.
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    public class ChangeRequest {

        public static final int REGISTER = 1;
        public static final int CHANGEOPS = 2;

        public SocketChannel socket;
        public int type;
        public int ops;

        public ChangeRequest(SocketChannel socket, int type, int ops) {
            this.socket = socket;
            this.type = type;
            this.ops = ops;
        }
    }

}
