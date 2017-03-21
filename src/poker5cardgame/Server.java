package poker5cardgame;

import poker5cardgame.network.GameServer;
import poker5cardgame.network.Server;

/**
 * Server launcher class
 */
public class Server {

    static poker5cardgame.network.Server server;
    
    static int bindPort = 1212;
    static int mode = 1;

    public static void main(String... args) {

        try {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];

                // Help argument
                if (arg.equals("-h")) {
                    System.out.println("Us: java Server -p <port> [-i 1|2]");
                    return;
                }

                // Port Argument
                if (arg.equals("-p")) {
                    String arg2 = args[++i];
                    bindPort = Integer.valueOf(arg2);
                }

                // AI Mode Argument
                if (arg.equals("-i")) {
                    String arg2 = args[++i];
                    mode = Integer.valueOf(arg2);
                }

            }
        } catch (Exception e) {
            System.err.println("Server: Exception reading paramenters:");
            e.printStackTrace();
            System.exit(1);
        }

        startServer();

    }

    public static void startServer() {
        System.out.println("Starting Server...");
        System.out.println("Port: " + bindPort);
        System.out.println("Interactive: " + mode);
        
        server = new GameServer();
        server.bind(bindPort);
        server.getGame();

    }
}
