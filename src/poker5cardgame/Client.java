package poker5cardgame;

import poker5cardgame.game.GameState;
import poker5cardgame.game.Move;
import poker5cardgame.network.GameClient;
import static poker5cardgame.Log.*;

/**
 * Client launcher class
 */
public class Client {

    static String remoteAddr = "localhost";
    static int remotePort = 1212;
    static int mode = 0;

    static GameClient client;

    public static void main(String... args) {

        try {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];

                // Help argument
                if (arg.equals("-h")) {
                    System.out.println("Us: java Client -s <maquina_servidora> -p <port> [-i 0|1|2]");
                    return;
                }

                // Server address argument
                if (arg.equals("-s")) {
                    remoteAddr = args[++i];
                }

                // Port Argument
                if (arg.equals("-p")) {
                    String arg2 = args[++i];
                    remotePort = Integer.valueOf(arg2);
                }

                // AI Mode Argument
                if (arg.equals("-i")) {
                    String arg2 = args[++i];
                    mode = Integer.valueOf(arg2);
                }

            }
        } catch (Exception e) {
            NET_ERROR("Client: Exception reading paramenters:");
            e.printStackTrace();
            System.exit(1);
        }

        startClient();

    }

    /**
     * Connect to a server and play
     */
    private static void startClient() {
        System.out.println("Starting Client...");
        System.out.println("Remote Addr: " + remoteAddr);
        System.out.println("Port: " + remotePort);
        System.out.println("Interactive: " + mode);

        GameClient client = new GameClient();
        client.connect(remoteAddr, remotePort);

        Move move = new Move();
        move.action = GameState.Action.START;
        move.id = 1234;
        client.getSource().sendMove(move);

        System.out.println(client.getSource().getNextMove());

    }

}
