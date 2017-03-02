package poker5cardgame.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable{

    ServerSocket serverSocket;
    
    public Server() {
        
    }
    
    public void bind(int port){
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            System.err.println("Fallo en crear socket");
        }
    }
    
    public void start(){
         new Thread(this).start();
    }

     @Override
    public void run() {
        while (true) {
            try {
                Socket client = serverSocket.accept();
                new Thread(new ServerRunnable(client)).start();

            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void close() {
    }
    
    
    private class ServerRunnable implements Runnable{
        
        ComUtils com;
               
        public ServerRunnable(Socket sock){
            com = new ComUtils(sock);
        }
        
        @Override
        public void run() {
            while(true){
                try {
                    String str = com.read_string_variable(4);
                    System.out.println("Client says: " + str);
                } catch (IOException ex) {
                    System.err.println("Client Failure");
                }
            }
        }
    }

}
