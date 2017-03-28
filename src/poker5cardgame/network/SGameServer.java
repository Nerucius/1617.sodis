/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.network;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import static poker5cardgame.Log.*;
import poker5cardgame.game.GameData;
import poker5cardgame.game.GameServer;
import poker5cardgame.io.ByteBufferInputStream;
import poker5cardgame.io.ByteBufferOutputStream;
import poker5cardgame.io.ComUtils;

/**
 * A Selector Server which manages multiple games.
 */
public class SGameServer extends SelectorServer {

    // Program-Wide Saved games
    private final ConcurrentHashMap<Integer, GameData> savedGames = new ConcurrentHashMap<>();
    private final HashMap<SocketChannel, GameServer> games = new HashMap<>();
    private final HashMap<SocketChannel, ClientStreams> clientStreams = new HashMap<>();

    // Slave ComUtils
    private final ComUtils comUtils = new ComUtils();

    public SGameServer() {
        this.worker = new GameWorker();
    }

    private class GameWorker extends SelectorWorker {

        @Override
        public void handleData(SelectorWorker.ServerDataEvent event) {
            NET_TRACE("SServer: Received " + new String(event.data));

            // Get or create a buffer for this socket
            ClientStreams streams;
            if (clientStreams.containsKey(event.socket))
                streams = clientStreams.get(event.socket);
            else {
                streams = new ClientStreams();
                streams.readBuffer = ByteBuffer.allocate(512);
                streams.writeBuffer = ByteBuffer.allocate(512);

                // Wrap read and write buffer
                streams.is = new ByteBufferInputStream(streams.readBuffer);
                streams.os = new ByteBufferOutputStream(streams.writeBuffer);
                clientStreams.put(event.socket, streams);
            }

            // TODO This is totally not working
            
            // if the last time there was no complete packet
            // Move write pointer to mark, put new data, mark
            if(streams.incomplete) streams.readBuffer.reset();
            streams.readBuffer.put(event.data);
            streams.readBuffer.mark();
            
            try {
                System.out.println("Reading inputStream");
                byte[] bytes = new byte[1];
                
                while ((streams.is.read(bytes, 0, 1) != -1)) {
                    System.out.print(new String(bytes));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            
            if(true) return;


            comUtils.setInputOutputStreams(streams.is, streams.os);
            Packet packet = comUtils.read_NetworkPacketSelector();
            if (packet == null) {
                // Received incomplete packet
                streams.incomplete = true;
                NET_TRACE("SServer: Incomplete packet: " + new String(event.data));
                return;
            }
            
            // Clear inclomplete flag
            streams.incomplete = false;
            
            // Clear write stream
            streams.writeBuffer.clear();
            comUtils.write_NetworkPacket(packet);
            
            byte[] sent = streams.writeBuffer.array();
            NET_TRACE("SServer: Sent: " + new String(sent));
            event.server.send(event.socket, sent);
            
        }

    }

    private class ClientStreams {

        public ByteBuffer readBuffer;
        public ByteBuffer writeBuffer;

        ByteBufferInputStream is;
        ByteBufferOutputStream os;
        
        boolean incomplete = false;

        public ClientStreams() {
        }

    }

}
