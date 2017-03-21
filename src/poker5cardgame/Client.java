package poker5cardgame;

import java.net.InetAddress;
import poker5cardgame.network.GameClient;

/**
 * Client launcher class
 */
public class Client {

    static InetAddress remoteAddr = null;
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
                    String arg2 = args[++i];
                    remoteAddr = InetAddress.getByName(arg2);
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
            System.err.println("Client: Exception reading paramenters:");
            e.printStackTrace();
            System.exit(1);
        }

        startClient();
        
    }
   
    
    /** Connect to a server and play */
    private static void startClient(){
        System.out.println("Starting Client...");
        System.out.println("Remote Addr: "+ remoteAddr.getHostAddress());
        System.out.println("Port: "+ remotePort);
        System.out.println("Interactive: " + mode);
        
    }

}
