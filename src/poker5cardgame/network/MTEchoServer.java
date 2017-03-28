/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker5cardgame.network;

import java.net.Socket;
import static poker5cardgame.Log.*;
import poker5cardgame.io.ComUtils;
import poker5cardgame.io.NetworkSource;
import poker5cardgame.io.Writable;
import poker5cardgame.network.Packet;

/**
 * Echoing server. Listens for any correctly formatted protocol message and
 * sends back an ERRO message with the parsed command and arguments.
 *
 * @author German Dempere
 */
public class MTEchoServer extends MultithreadServer {

    NetworkSource source;
    Socket client;

    @Override
    public void handleConnection(Socket client) {
        this.client = client;
        source = new NetworkSource(client);
        ComUtils com = source.getCom();

        // Echo loop for one client
        while (true) {

            Packet packet = com.read_NetworkPacket();

            if (packet.command == Network.Command.NET_ERROR) {
                NET_ERROR("Server: Network Error for " + client.getInetAddress());
                break;
            }

            // Copy entire packet to a new String
            StringBuilder sb = new StringBuilder();
            sb.append(packet.command.code);
            for (Packet.Entry e : packet.getFields())
                sb.append(" ").append(e.writable.toString());

            Packet reply = new Packet(Network.Command.ERROR);
            reply.putWrittable(new Writable.VariableString(2, sb.toString()));

            // Send Packet
            com.write_NetworkPacket(reply);

            NET_DEBUG("EchoServer: Replied to packet " + packet + ": " + reply);


            /* Move Testing Code
            Game.Move m = source.getNextMove();
            if (m.action == Game.Action.TERMINATE) {
                break;
            }
            System.out.println("Server: Echoing " + m.action);

            Game.Move echo = new Game.Move();
            echo.action = Game.Action.ERROR;
            echo.error = m.toString();
            source.sendMove(echo);
             */
        }

    }

}
