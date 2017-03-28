/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import poker5cardgame.game.GameData;
import poker5cardgame.game.ServerGame;
import poker5cardgame.io.ComUtils;

import static poker5cardgame.Log.*;
import poker5cardgame.ai.ArtificialIntelligence;
import poker5cardgame.io.NetworkSource;

/**
 * A Selector Server which manages multiple games.
 */
public class SGameServer extends SelectorServer {

    private ArtificialIntelligence.Type AIType = ArtificialIntelligence.Type.AI_RANDOM;
    
    // Program-Wide Saved games
    private final ConcurrentHashMap<Integer, GameData> savedGames = new ConcurrentHashMap<>();
    private final HashMap<SocketChannel, ClientCapsule> clientStreams = new HashMap<>();

    // Slave ComUtils and Network source
    private final ComUtils comUtils = new ComUtils();

    public SGameServer() {
        this.worker = new GameWorker();
    }

    private class GameWorker extends SelectorWorker {

        @Override
        public void handleData(SelectorWorker.ServerDataEvent event) {
            NET_TRACE("SServer: Received " + new String(event.data));

            Packet packet = defragmentPacket(event);

            // Null packet, ignore for now
            if (packet == null)
                return;

            // Get the streams for this client
            ClientCapsule cc = clientStreams.get(event.socket);
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
            while(keepUpdating)
                keepUpdating = !game.update();

            //comUtils.write_NetworkPacket(packet);
            // Get whatever the Server wanted to write out and send it
            byte[] dataSent = cc.os.toByteArray();
            event.server.send(event.socket, dataSent);

        }

    }

    private Packet defragmentPacket(SelectorWorker.ServerDataEvent event) {
        // Get or create a buffer for this socket
        ClientCapsule streams = getClient(event);

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

        NET_DEBUG("SServer: Got complete packet");

        return packet;
    }

    /**
     * Finds or creates a new client capsule for the given socket channel.
     *
     * @param event
     * @return Found or New Client Capsule
     */
    private ClientCapsule getClient(SelectorWorker.ServerDataEvent event) {
        ClientCapsule client;

        if (clientStreams.containsKey(event.socket))
            client = clientStreams.get(event.socket);

        else {
            client = new ClientCapsule();

            // Create and wrap read buffer
            client.readBuffer = ByteBuffer.allocate(512);
            client.is = new ByteArrayInputStream(client.readBuffer.array());

            // Create output buffer
            client.os = new ByteArrayOutputStream(512);
            clientStreams.put(event.socket, client);

            // Create networkSource and Game
            client.nSource = new NetworkSource();
            client.nSource.setBlocking(false);
            client.game = new ServerGame(client.nSource, AIType);
            client.game.isSelector = true;
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
