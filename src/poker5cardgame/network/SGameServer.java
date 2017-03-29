package poker5cardgame.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import poker5cardgame.game.ServerGame;
import poker5cardgame.io.ComUtils;

import static poker5cardgame.Log.*;
import poker5cardgame.ai.ArtificialIntelligence;
import poker5cardgame.game.GameData;
import poker5cardgame.io.NetworkSource;

/**
 * A Selector Server which manages multiple games.
 */
public class SGameServer extends SelectorServer {

    private ArtificialIntelligence.Type AIType = ArtificialIntelligence.Type.AI_RANDOM;

    // Program-Wide Saved games
    private final HashMap<SocketChannel, ClientCapsule> clientStreams = new HashMap<>();
    private final HashMap<Integer, GameData> savedGames = new HashMap<>();

    // Slave ComUtils
    private final ComUtils comUtils = new ComUtils();

    public SGameServer() {
        this.worker = new GameWorker();
    }

    private class GameWorker extends SelectorWorker {

        /** Handle Incoming data from the network on a given Socket Channel */
        @Override
        public void handleData(SelectorWorker.ServerDataEvent event) {
            NET_TRACE("SServer: Received " + new String(event.data));

            Packet packet = defragmentPacket(event);

            // Null packet, means the packet could not be read fully, ignore for now
            if (packet == null)
                return;

            // Get the streams for this client
            ClientCapsule cc = getClient(event, packet);
            ServerGame game = cc.game;

            cc.nSource.addPacketToQueue(packet);

            // Special case for Moves that require two packets. Exit now
            if (packet.command == Network.Command.ANTE || packet.command == Network.Command.DEALER)
                return;

            // Prepare streams to receive new output data from the Game
            cc.os.reset();
            comUtils.setInputOutputStreams(cc.is, cc.os);
            cc.nSource.setComUtils(comUtils);

            // Update the game
            game.nextMoveReady = true;
            boolean keepUpdating = true;
            while (keepUpdating)
                keepUpdating = !game.update();

            
            // Get whatever the Server wanted to write out and send it
            byte[] dataSent = cc.os.toByteArray();
            event.server.send(event.socket, dataSent);

            // Save game data for this client
            savedGames.put(cc.game.getID(), cc.game.getGameData());

        }

    }

    private Packet defragmentPacket(SelectorWorker.ServerDataEvent event) {
        // Get or create a buffer for this socket
        ClientCapsule streams = getClient(event, null);

        // if the last time there was no complete packet reset the input
        // stream's read pointer, and append new data
        if (streams.incomplete)
            streams.is.reset();
        streams.readBuffer.put(event.data);

        // Try to read a complete packet
        comUtils.setInputOutputStreams(streams.is, streams.os);
        Packet packet = comUtils.read_NetworkPacketSelector();

        if (packet == null) {
            // Received incomplete packet
            streams.incomplete = true;
            NET_DEBUG("SServer: Incomplete packet: " + new String(event.data));
            return null;
        }

        // Clear inclomplete flag
        streams.incomplete = false;

        // Clear write stream
        streams.is.reset();
        streams.readBuffer.clear();

        NET_TRACE("SServer: Got complete packet " + packet);

        return packet;
    }

    /**
     * Finds or creates a new client capsule for the given socket channel.
     *
     * @param event
     * @return Found or New Client Capsule
     */
    private ClientCapsule getClient(SelectorWorker.ServerDataEvent event, Packet packet) {
        ClientCapsule client;

        if (clientStreams.containsKey(event.socket)) {
            client = clientStreams.get(event.socket);

            // Persitence
            if (packet != null && packet.command == Network.Command.START) {
                
                // See if this client has any previous saved games
                int gameID = packet.getField("id", Integer.class);
                if (savedGames.containsKey(gameID)) {
                    // Create a new game but copy old data
                    client.game = new ServerGame(client.nSource, AIType);
                    client.game.isSelector = true;
                    client.game.nextMoveReady = false;
                    client.game.copyGameData(savedGames.get(gameID));
                }
            }
            
        } else {
            client = new ClientCapsule();

            // Create and wrap read buffer
            client.readBuffer = ByteBuffer.allocate(512);
            client.is = new ByteArrayInputStream(client.readBuffer.array());

            // Create output buffer
            client.os = new ByteArrayOutputStream(512);

            // Create networkSource and Game
            client.nSource = new NetworkSource();
            client.nSource.setBlocking(false);
            client.game = new ServerGame(client.nSource, AIType);
            client.game.isSelector = true;
            
            // Save to HashMap
            clientStreams.put(event.socket, client);
        }

        return client;
    }

    /**
     * A Container class that encapsulates all that is necessary for
     * communicating with a recurring client.
     */
    private class ClientCapsule {

        // Read/Write buffers
        public ByteBuffer readBuffer;
        public ByteArrayInputStream is;
        public ByteArrayOutputStream os;

        // Private networkSource and Game
        ServerGame game;
        NetworkSource nSource;

        // Set to true if the last transmission was a fragment
        boolean incomplete = false;
    }

    @Override
    public void setAIType(ArtificialIntelligence.Type type) {
        this.AIType = type;
    }

}
