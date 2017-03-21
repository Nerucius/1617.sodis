package poker5cardgame;

/**
 * Server launcher class
 */
public class Server {
    
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

        return;
    }

    public static void startServer() {

    }
}
